package com.ultreon.craft.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.c2s.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.s2c.S2CDisconnectPacket;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.stage.PacketStages;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.text.TextObject;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
    public static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    public static final AttributeKey<PacketData<ServerPacketHandler>> DATA_TO_SERVER_KEY = AttributeKey.valueOf("data_to_server");
    public static final AttributeKey<PacketData<ClientPacketHandler>> DATA_TO_CLIENT_KEY = AttributeKey.valueOf("data_to_client");

    private final PacketDestination direction;
    @Nullable
    private Channel channel;
    @Nullable
    private SocketAddress remoteAddress;
    private final Queue<Runnable> tasks = Queues.newArrayDeque();
    private String disconnectMsg;

    public static final Supplier<NioEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(Connection::createLocalWorkerGroup);
    public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(Connection::createNetworkEpollWorkerGroup);

    public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(Connection::createNetworkWorkerGroup);

    private PacketHandler handler;
    private PacketHandler disconnectHandler;
    private boolean handlingFault;
    private final PacketContext context;
    boolean memoryConnection;
    private boolean connecting = true;
    private boolean disconnected = false;
    private String address;
    private int port;
    private final ExecutorService dispatchExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 3, 1));
    private ServerPlayer player;
    private int keepAlive = 100;
    private long ping;
    private boolean disconnecting = false;

    public Connection(PacketDestination direction) {
        this.direction = direction;
        this.context = new PacketContext(null, this, direction.getSourceEnv());
    }

    private static NioEventLoopGroup createLocalWorkerGroup() {
        return new NioEventLoopGroup(1, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
    }

    private static NioEventLoopGroup createNetworkWorkerGroup() {
        return new NioEventLoopGroup(8, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build());
    }

    private static EpollEventLoopGroup createNetworkEpollWorkerGroup() {
        return new EpollEventLoopGroup(8, new ThreadFactoryBuilder().setNameFormat("Netty Network Client IO #%d").setDaemon(true).build());
    }

    public static int getPacketsSent() {
        return ValueTracker.getPacketsSent();
    }

    public static int getPacketsReceived() {
        return ValueTracker.getPacketsReceived();
    }

    public static int getPacketsReceivedTotal() {
        return ValueTracker.getPacketsReceivedTotal();
    }

    private static void closeFail(Future<? super Void> future) {
        if (!(future.cause() instanceof ClosedChannelException))
            Connection.LOGGER.error("Failed to close channel", future.cause());
    }

    public void delayDisconnect(String message) {
        this.disconnectMsg = message;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        this.channel = ctx.channel();
        this.remoteAddress = this.channel.remoteAddress();

        if (this.direction.getSourceEnv() == EnvType.CLIENT) {
            Connection.LOGGER.info("Connected to: {}", this.remoteAddress != null ? this.remoteAddress : "null");
        }

        if (this.shouldDisconnect()) {
            this.disconnect(this.disconnectMsg);
            return;
        }

        this.pollAll();

        this.connecting = false;
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        this.disconnect("End of stream");
    }

    @ApiStatus.Internal
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ClosedChannelException) return;

        try {
            boolean handlingFault = !this.handlingFault;
            this.handlingFault = true;

            Channel channel = this.channel;
            if (channel != null && channel.isOpen()) {
                if (cause instanceof TimeoutException) {
                    Connection.LOGGER.debug("Timeout", cause);
                    this.disconnect("Timed Out");
                } else {
                    this.handleInternalError(cause, handlingFault, channel);
                }
            }
        } catch (Exception e) {
            Connection.LOGGER.error("Failed to handle exception", e);
        }
    }

    private void handleInternalError(Throwable cause, boolean handlingFault, Channel channel) {
        Connection.LOGGER.error("Exception: ", cause);
        this.disconnect("Internal Exception: " + cause);
        if (handlingFault) {
            Connection.LOGGER.error("Double fault detected, force closing connection.");
            try {
                channel.close().addListener(Connection::closeFail);
            } catch (Exception e) {
                Connection.LOGGER.error("Failed to close channel", e);
            }
        }
    }

    public void setReadOnly() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }
    }

    private boolean shouldDisconnect() {
        return this.disconnectMsg != null;
    }

    /**
     * Disconnects the connection with the given message.
     */
    public void disconnect() {
        disconnect("Disconnected");
    }

    /**
     * Disconnects the connection with the given message.
     *
     * @param message The message to display when disconnecting.
     */
    public void disconnect(@NotNull TextObject message) {
        this.disconnect(message.getText());
    }

    /**
     * Disconnects the connection with the given message.
     *
     * @param message The message to display when disconnecting.
     */
    public void disconnect(@NotNull String message) {
        // Check if the channel is null, not open, or already disconnecting
        if (this.channel == null || !this.channel.isOpen() || this.disconnecting) {
            return;
        }

        this.disconnecting = true;

        @NotNull String msg = "Disconnected: ";

        // Log the disconnection message
        Connection.LOGGER.info("%s%s (%s)".formatted(msg, this.remoteAddress != null ? this.remoteAddress.toString() : null, message));

        // Send the appropriate disconnect packet based on the connection direction
        if (this.direction.getSourceEnv() == EnvType.SERVER) {
            this.send(new S2CDisconnectPacket<>(message), PacketResult.onEither(this::closeAll));
        } else if (this.direction.getSourceEnv() == EnvType.CLIENT) {
            this.send(new C2SDisconnectPacket<>(message), PacketResult.onEither(this::closeAll));
        }

        // Set the disconnect message
        this.disconnectMsg = message;

        // Handle the disconnection
        this.handleDisconnect();

        // Set the connection to read-only mode
        this.setReadOnly();
    }

    /**
     * Sends a packet.
     *
     * @param packet The packet to send
     */
    public void send(@NotNull Packet<?> packet) {
        this.send(packet, true);
    }

    /**
     * Sends a packet with an option to flush.
     *
     * @param packet The packet to send
     * @param flush Whether to flush the packet
     */
    public void send(@NotNull Packet<?> packet, boolean flush) {
        this.send(packet, null, flush);
    }

    /**
     * Sends a packet with an optional state listener and an option to flush.
     *
     * @param packet The packet to send
     * @param stateListener The listener for packet state
     */
    public void send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener) {
        this.send(packet, stateListener, true);
    }

    /**
     * Sends the packet, with an optional state listener, and flushes the data if specified.
     *
     * @param packet        the packet to send
     * @param stateListener the optional state listener
     * @param flush         true if the data should be flushed
     */
    public void send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) {
        if (!this.isConnected()) {
            // If not connected, add the task to send the packet once connected
            this.tasks.add(() -> {
                try {
                    this._send(packet, stateListener, flush);
                } catch (Exception e) {
                    Connection.LOGGER.error(CommonConstants.EX_FAILED_TO_SEND_PACKET, e);
                }
            });
            return;
        }

        // Poll all tasks before sending the packet
        this.pollAll();

        try {
            // Send the packet
            this._send(packet, stateListener, flush);
        } catch (Exception e) {
            Connection.LOGGER.error(CommonConstants.EX_FAILED_TO_SEND_PACKET, e);
        }
    }

    /**
     * Sends a packet through the channel, handling cases where the channel is closed or not in the event loop.
     *
     * @param packet        The packet to send
     * @param stateListener Optional listener for packet result
     * @param flush         Whether to flush the packet
     * @throws ClosedChannelException If the channel is closed
     */
    private void _send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) throws ClosedChannelException {
        // Check if the channel is null
        if (this.channel == null) {
            return;
        }

        // Throw exception if channel is not open
        if (!this.channel.isOpen()) throw new ClosedChannelException();

        // If in the event loop, directly send the packet
        if (this.channel.eventLoop().inEventLoop()) {
            this._actuallySend(packet, stateListener, flush);
        } else {
            // If not in event loop, execute sending on the event loop
            this.channel.eventLoop().execute(() -> {
                try {
                    this._actuallySend(packet, stateListener, flush);
                } catch (Exception e) {
                    Connection.LOGGER.error(CommonConstants.EX_FAILED_TO_SEND_PACKET, e);
                }
            });
        }
    }

    /**
     * Actually send a packet over a channel with optional flushing and state listener handling.
     *
     * @param packet        The packet to be sent
     * @param stateListener The listener for packet sending state
     * @param flush         Whether to flush the channel
     */
    private void _actuallySend(Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) {
        Preconditions.checkNotNull(packet, "packet");

        try {
            // Check if channel is available
            if (this.channel == null) {
                Connection.LOGGER.warn("Can't send packet because the channel isn't available.");
                return;
            }
            // Check if channel is open
            if (!this.channel.isOpen()) return;

            // Increment packets sent count
            ValueTracker.setPacketsSent(ValueTracker.getPacketsSent() + 1);

            // Log sending packet if packet logging is enabled
            if (DebugFlags.PACKET_LOGGING.enabled())
                Connection.LOGGER.debug("Sending packet: {}", packet.getClass().getName());

            // Send packet with or without flushing based on 'flush' parameter
            ChannelFuture sent = flush ? this.channel.writeAndFlush(packet) : this.channel.write(packet);

            // Fire exception on failure
            sent.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

            // Handle state listener if present
            if (stateListener != null) {
                sent.addListener(future -> this.handleListener(packet, stateListener, future));
            }
        } catch (Exception e) {
            // Log error if sending packet fails
            Connection.LOGGER.error("Failed to send packet: {}", packet.getClass().getName(), e);
        }
    }

    /**
     * Handles the listener for a packet.
     *
     * @param packet        the packet to handle
     * @param stateListener the listener for the packet result
     * @param future        the future result of sending the packet
     */
    private void handleListener(Packet<?> packet, @NotNull PacketResult stateListener, Future<? super Void> future) {
        try {
            Channel ch = this.channel;

            // Check if the channel is available
            if (ch == null) {
                Connection.LOGGER.warn("Can't handle packet because the channel isn't available.");
                return;
            }

            // Check if sending the packet was successful
            if (future.isSuccess()) {
                stateListener.onSuccess();
                return;
            }

            // Check if the failure was due to a closed channel
            if (future.cause() instanceof ClosedChannelException) {
                return;
            }

            // Log the failure to send the packet
            Connection.LOGGER.warn("Failed to send packet: " + packet.getClass().getName(), future.cause());

            // Get a packet for failure response
            Packet<?> failPacket = stateListener.onFailure();

            // Send the fail packet if available
            if (failPacket != null) {
                ChannelFuture finalAttempt = ch.writeAndFlush(failPacket);
                finalAttempt.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        } catch (Exception e) {
            // Log any exceptions that occur during handling
            Connection.LOGGER.error("Failed to handle response: " + packet.getClass().getName(), e);
        }
    }

    /**
     * Checks if the connection is currently connected to a server or the client.
     *
     * @return {@code true} if the connection is currently connected, {@code false} otherwise
     */
    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) {
        ValueTracker.setPacketsReceivedTotal(ValueTracker.getPacketsReceivedTotal() + 1);
        if (this.channel != null && this.channel.isOpen()) {
            PacketHandler handler = this.handler;
            if (handler == null) {
                Connection.LOGGER.error("Packet handler isn't set yet!");
                return;
            }
            try {
                ValueTracker.setPacketsReceived(ValueTracker.getPacketsReceived() + 1);
                this.readGeneric(msg, handler);
            } catch (RejectedExecutionException ex) {
                Connection.LOGGER.error("Packet handler rejected the packet:", ex);
                this.disconnect("Server shutdown");
            } catch (ClassCastException ex) {
                Connection.LOGGER.error("Received {} that couldn't be processed", msg.getClass(), ex);
                this.disconnect("Server sent an invalid packet.");
            } catch (Exception ex) {
                Connection.LOGGER.error("Read error:", ex);
                this.disconnect("The receiver couldn't read the packet:\n" + ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PacketHandler> void readGeneric(@NotNull Packet<T> msg, @NotNull PacketHandler handler) {
        PacketContext context = handler.context();
        if (context == null) context = this.context;
        PacketContext finalContext = context;
        if (handler.isDisconnected()) return;
        if (DebugFlags.PACKET_LOGGING.enabled())
            CommonConstants.LOGGER.debug("Received packet: {}", msg.getClass().getSimpleName());

        if (handler.isAsync()) {
            CompletableFuture.runAsync(() -> {
                try {
                    if (handler.isDisconnected()) return;
                    msg.handle(finalContext, (T) handler);
                } catch (Exception e) {
                    Connection.LOGGER.error("Failed to handle packet:", e);
                }
            }, this.dispatchExecutor);
        } else {
            try {
                if (handler.isDisconnected()) return;
                msg.handle(finalContext, (T) handler);
            } catch (Exception e) {
                Connection.LOGGER.error("Failed to handle packet:", e);
            }
        }
    }

    private void pollAll() {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.tasks) {
                Runnable task;
                while ((task = this.tasks.poll()) != null) {
                    task.run();
                }
            }
        }
    }

    /**
     * Gets the direction of the connection.
     *
     * @return the direction of the connection
     */
    public PacketDestination getDirection() {
        return this.direction;
    }

    /**
     * Gets the current environment of the connection.
     *
     * @return the current environment using Fabric's {@link EnvType}
     */
    public EnvType getCurrentEnv() {
        return this.direction.getSourceEnv();
    }

    /**
     * Sets the initial attributes for the connection.
     *
     * @param channel the Netty channel to set the attributes on.
     */
    public static void setInitAttributes(Channel channel) {
        channel.attr(Connection.DATA_TO_CLIENT_KEY).set(PacketStages.LOGIN.getClientBoundData());
        channel.attr(Connection.DATA_TO_SERVER_KEY).set(PacketStages.LOGIN.getServerData());

        channel.config().setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(64, 8192, 1024 * 1024 * 2));
    }

    /**
     * Moves to the in-game connection stage.
     */
    public void moveToInGame() {
        if (this.channel != null && this.channel.isOpen()) {
            this.channel.attr(Connection.DATA_TO_CLIENT_KEY).set(PacketStages.IN_GAME.getClientBoundData());
            this.channel.attr(Connection.DATA_TO_SERVER_KEY).set(PacketStages.IN_GAME.getServerData());
        }
    }

    /**
     * Sets up the pipeline to handle packets.
     * Adds the packet encoding and decoding, and optionally ZLib compression (when this is not a memory connection).
     *
     * @param pipeline the pipeline to set up
     */
    @ApiStatus.Internal
    public void setup(ChannelPipeline pipeline) {
        try {
            var oppositeDirection = this.direction.opposite();
            var ourData = Connection.getDataKey(this.direction);
            var theirData = Connection.getDataKey(oppositeDirection);

            pipeline.addLast("field_decoder", new LengthFieldBasedFrameDecoder(1024 * 1024 * 8, 0, 4, 0, 4))
                    .addLast("field_prepender", new LengthFieldPrepender(4));
            pipeline.addLast("decoder", new PacketDecoder(theirData))
                    .addLast("encoder", new PacketEncoder(ourData));
            if (!this.isMemoryConnection()) {
                pipeline.addLast("zlib_decoder", new JdkZlibDecoder())
                        .addLast("zlib_encoder", new JdkZlibEncoder());
            }
        } catch (Exception e) {
            Connection.LOGGER.error("Failed to setup:", e);
            throw e;
        }
    }

    /**
     * Sets up the packet handler for the pipeline
     *
     * @param pipeline the pipeline
     */
    public void setupPacketHandler(ChannelPipeline pipeline) {
        pipeline.addLast("handler", this);
    }

    private static AttributeKey<? extends PacketData<?>> getDataKey(PacketDestination direction) {
        return switch (direction) {
            case CLIENT -> Connection.DATA_TO_CLIENT_KEY;
            case SERVER -> Connection.DATA_TO_SERVER_KEY;
        };
    }

    /**
     * Set the active packet handler
     *
     * @param handler the handler
     */
    public void setHandler(PacketHandler handler) {
        this.handler = handler;
    }

    /**
     * Get the remote address of the connection
     *
     * @return the remote address
     */
    public @Nullable SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    /**
     * Queues a runnable to be executed on the server/client thread.
     *
     * @param handler the runnable
     */
    public void queue(Runnable handler) {
        this.tasks.offer(handler);
    }

    /**
     * Initiates the connection.
     *
     * @param address     the address to connect to
     * @param port        the port to connect to
     * @param handler     the handler to use for the connection
     * @param loginPacket the login packet to send once connected
     */
    public void initiate(String address, int port, PacketHandler handler, Packet<?> loginPacket) {
        this.disconnectHandler = handler;
        this.address = address;
        this.port = port;
        Connection.LOGGER.info("Initiated connection to {}:{}", this.address, this.port);
        this.runOnceConnected(() -> {
            this.setHandler(handler);
            this.send(loginPacket, true);
        });
    }

    /**
     * Runs the consumer once the connection is connected.
     *
     * @param consumer the consumer to run
     */
    public void runOnceConnected(Runnable consumer) {
        if (this.isConnected()) {
            this.pollAll();
            consumer.run();
            return;
        }
        this.tasks.add(consumer);
    }

    /**
     * Checks if the connection is a memory connection.
     *
     * @return true if the connection is a memory connection, false otherwise
     */
    public boolean isMemoryConnection() {
        return this.memoryConnection;
    }

    /**
     * Checks if the connection is currently connecting.
     *
     * @return true if the connection is currently connecting, false otherwise
     */
    public boolean isConnecting() {
        return this.connecting;
    }

    /**
     * Ticks the connection.
     */
    @ApiStatus.Internal
    public void tick() {
        this.pollAll();

        if (!this.isConnected() && !this.disconnected) {
            this.handleDisconnect();
        }

        if (this.channel != null) {
            this.channel.flush();
        }
    }

    @ApiStatus.Internal
    public void handleDisconnect() {
        if (!this.disconnected) {
            this.disconnected = true;
            PacketHandler handler = this.handler;
            PacketHandler finalHandler = handler != null ? handler : this.disconnectHandler;
            if (finalHandler == null) return;
            if (finalHandler.isDisconnected()) return;

            String message = this.disconnectMsg;
            if (message == null) message = "Connection lost";


            finalHandler.onDisconnect(message);

            if (this.player != null) {
                UltracraftServer server = this.player.getWorld().getServer();
                server.onDisconnected(this.player, message);
            }
        }
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public ChannelFuture close() {
        this.dispatchExecutor.shutdownNow();
        return this.closeChannel();
    }

    public Future<?> closeGroup() {
        EventLoopGroup group;
        if (this.isMemoryConnection()) {
            group = Connection.LOCAL_WORKER_GROUP.get();
        } else {
            group = Connection.NETWORK_WORKER_GROUP.get();
        }
        return group.shutdownGracefully();
    }

    @Nullable
    private ChannelFuture closeChannel() {
        Channel channel = this.channel;
        if (channel != null)
            return channel.isOpen() ? channel.close() : channel.newSucceededFuture();

        return null;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * Tick the keep-alive timer, and returns true if the keep-alive packet should be sent.
     *
     * @return true, if the keep-alive packet should be sent.
     */
    @ApiStatus.Internal
    public boolean tickKeepAlive() {
        boolean doKeepAlive = this.keepAlive-- <= 0;
        if (doKeepAlive) this.keepAlive = 100;
        return doKeepAlive;
    }

    public void closeAll() {
        this.close();
        this.closeGroup();
    }

    public void onPing(long ping) {
        this.ping = ping;
    }

    public long getPing() {
        return this.ping;
    }
}