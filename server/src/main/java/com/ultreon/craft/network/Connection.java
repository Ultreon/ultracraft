package com.ultreon.craft.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.c2s.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.s2c.S2CDisconnectPacket;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.stage.PacketStages;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
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
    private static int packetsReceived;
    private static int packetsReceivedTotal;

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
    private static int packetsSent;
    private final ExecutorService dispatchExecutor = Executors.newFixedThreadPool(8);
    private ServerPlayer player;
    private int keepAlive = 100;

    public Connection(PacketDestination direction) {
        this.direction = direction;
        this.context = new PacketContext(null, this, direction.getSourceEnv());
    }

    private static NioEventLoopGroup createLocalWorkerGroup() {
        return new NioEventLoopGroup(8, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
    }

    private static NioEventLoopGroup createNetworkWorkerGroup() {
        return new NioEventLoopGroup(8, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build());
    }

    private static EpollEventLoopGroup createNetworkEpollWorkerGroup() {
        return new EpollEventLoopGroup(8, new ThreadFactoryBuilder().setNameFormat("Netty Network Client IO #%d").setDaemon(true).build());
    }

    public static int getPacketsSent() {
        return Connection.packetsSent;
    }

    public static int getPacketsReceived() {
        return Connection.packetsReceived;
    }

    public static int getPacketsReceivedTotal() {
        return Connection.packetsReceivedTotal;
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
            Connection.LOGGER.info("Connected to: " + (this.remoteAddress != null ? this.remoteAddress : "null"));
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            boolean handlingFault = !this.handlingFault;
            this.handlingFault = true;

            Channel channel = this.channel;
            if (channel != null && channel.isOpen()) {
                if (cause instanceof TimeoutException) {
                    Connection.LOGGER.debug("Timeout", cause);
                    this.disconnect("Timed Out");
                } else {
                    Connection.LOGGER.error("Exception: ", cause);
                    this.disconnect("Internal Exception: " + cause);
                    if (handlingFault) {
                        Connection.LOGGER.error("Double fault detected, force closing connection.");
                        try {
                            channel.close().addListener(future -> {
                                if (!(future.cause() instanceof ClosedChannelException)) {
                                    Connection.LOGGER.error("Failed to close channel", future.cause());
                                }
                            });
                        } catch (Exception e) {
                            Connection.LOGGER.error("Failed to close channel", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Connection.LOGGER.error("Failed to handle exception", e);
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

    public void disconnect(@NotNull String message) {
        if (this.channel == null || !this.channel.isOpen()) return;

        @NotNull String msg = "Disconnected: ";
        Connection.LOGGER.info(msg + (this.remoteAddress != null ? this.remoteAddress.toString() : null) + " (" + message + ")");

        switch (this.direction.getSourceEnv()) {
            case SERVER -> this.send(new S2CDisconnectPacket<>(message), PacketResult.onEither(this::close));
            case CLIENT -> this.send(new C2SDisconnectPacket<>(message), PacketResult.onEither(this::close));
        }

        this.disconnectMsg = message;
        this.handleDisconnect();

        this.setReadOnly();
    }

    public void send(@NotNull Packet<?> packet) {
        this.send(packet, true);
    }

    public void send(@NotNull Packet<?> packet, boolean flush) {
        this.send(packet, null, flush);
    }

    public void send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener) {
        this.send(packet, stateListener, true);
    }

    public void send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) {
        if (!this.isConnected()) {
            this.tasks.add(() -> {
                try {
                    this._send(packet, stateListener, flush);
                } catch (Exception e) {
                    Connection.LOGGER.error("Failed to send packet:", e);
                }
            });
            return;
        }

        this.pollAll();

        try {
            this._send(packet, stateListener, flush);
        } catch (Exception e) {
            Connection.LOGGER.error("Failed to send packet:", e);
        }
    }

    private void _send(@NotNull Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) throws ClosedChannelException {
        if (this.channel == null) {
            return;
        }
        if (!this.channel.isOpen()) throw new ClosedChannelException();

        if (this.channel.eventLoop().inEventLoop()) {
            this._actuallySend(packet, stateListener, flush);
        } else {
            this.channel.eventLoop().execute(() -> {
                try {
                    this._actuallySend(packet, stateListener, flush);
                } catch (Exception e) {
                    Connection.LOGGER.error("Failed to send packet:", e);
                }
            });
        }
    }

    private void _actuallySend(Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) {
        try {
            Preconditions.checkNotNull(packet, "packet");
            if (this.channel == null) {
                Connection.LOGGER.warn("Can't send packet because the channel isn't available.");
                return;
            }
            if (!this.channel.isOpen()) throw new ClosedChannelException();

            ChannelFuture sent = flush ? this.channel.writeAndFlush(packet) : this.channel.write(packet);

            sent.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

            Connection.packetsSent++;

            if (stateListener != null) {
                sent.addListener(future -> {
                    try {
                        if (future.isSuccess()) {
                            stateListener.onSuccess();
                            return;
                        }

                        Connection.LOGGER.warn("Failed to send packet: " + packet.getClass().getName(), future.cause());
                        Packet<?> failPacket = stateListener.onFailure();
                        if (failPacket != null) {
                            ChannelFuture finalAttempt = this.channel.writeAndFlush(failPacket);
                            finalAttempt.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                        }
                    } catch (Exception e) {
                        Connection.LOGGER.error("Failed to handle response: " + packet.getClass().getName(), e);
                    }
                });
            }
        } catch (Exception e) {
            Connection.LOGGER.error("Failed to send packet: " + packet.getClass().getName(), e);
        }
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) {
        Connection.packetsReceivedTotal++;
        if (this.channel != null && this.channel.isOpen()) {
            PacketHandler handler = this.handler;
            if (handler == null) {
                Connection.LOGGER.error("Packet handler isn't set yet!");
                return;
            }
            try {
                Connection.packetsReceived++;
                this.readGeneric(msg, handler);
            } catch (RejectedExecutionException ex) {
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
    private <T extends PacketHandler> void readGeneric(Packet<T> msg, PacketHandler handler) {
        PacketContext context = handler.context();
        if (context == null) context = this.context;
        PacketContext finalContext = context;
        if (handler.isAsync()) {
            CompletableFuture.runAsync(() -> {
                try {
                    msg.handle(finalContext, (T) handler);
                } catch (Exception e) {
                    Connection.LOGGER.error("Failed to handle packet:", e);
                }
            }, this.dispatchExecutor);
        } else {
            msg.handle(finalContext, (T) handler);
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

    public PacketDestination getDirection() {
        return this.direction;
    }

    public EnvType getCurrentEnv() {
        return this.direction.getSourceEnv();
    }

    public static void setInitAttributes(Channel channel) {
        channel.attr(Connection.DATA_TO_CLIENT_KEY).set(PacketStages.LOGIN.getClientBoundData());
        channel.attr(Connection.DATA_TO_SERVER_KEY).set(PacketStages.LOGIN.getServerData());

        channel.config().setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(64, 8192, 1024 * 1024 * 2));
    }

    public void moveToInGame() {
        if (this.channel != null && this.channel.isOpen()) {
            this.channel.attr(Connection.DATA_TO_CLIENT_KEY).set(PacketStages.IN_GAME.getClientBoundData());
            this.channel.attr(Connection.DATA_TO_SERVER_KEY).set(PacketStages.IN_GAME.getServerData());
        }
    }

    public void setup(ChannelPipeline pipeline) {
        try {
            var oppositeDirection = this.direction.opposite();
            var ourData = Connection.getDataKey(this.direction);
            var theirData = Connection.getDataKey(oppositeDirection);

            pipeline.addLast("field_decoder", new LengthFieldBasedFrameDecoder(1024 * 1024 * 8, 0, 4, 0, 4))
                    .addLast("field_prepender", new LengthFieldPrepender(4))
                    .addLast("decoder", new PacketDecoder(theirData))
                    .addLast("encoder", new PacketEncoder(ourData))
            ;
        } catch (Throwable t) {
            Connection.LOGGER.error("Failed to setup:", t);
            throw t;
        }
    }

    public void setupPacketHandler(ChannelPipeline pipeline) {
        pipeline.addLast("handler", this);
    }

    private static AttributeKey<? extends PacketData<?>> getDataKey(PacketDestination direction) {
        return switch (direction) {
            case CLIENT -> Connection.DATA_TO_CLIENT_KEY;
            case SERVER -> Connection.DATA_TO_SERVER_KEY;
        };
    }

    public void setHandler(PacketHandler handler) {
        this.handler = handler;
    }

    public @Nullable SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public void queue(Runnable handler) {
        this.tasks.offer(handler);
    }

    public void initiate(String address, int port, PacketHandler handler, Packet<?> loginPacket) {
        this.disconnectHandler = handler;
        this.address = address;
        this.port = port;
        System.out.println("Initiated connection to " + address + ":" + port);
        this.runOnceConnected(() -> {
            this.setHandler(handler);
            this.send(loginPacket, true);
        });
    }

    public void runOnceConnected(Runnable consumer) {
        if (this.isConnected()) {
            this.pollAll();
            consumer.run();
            return;
        }
        this.tasks.add(consumer);
    }

    public boolean isMemoryConnection() {
        return this.memoryConnection;
    }

    public boolean isConnecting() {
        return this.connecting;
    }

    public void tick() {
        this.pollAll();

        if (!this.isConnected() && !this.disconnected) {
            this.handleDisconnect();
        }

        if (this.channel != null) {
            this.channel.flush();
        }
    }

    public void handleDisconnect() {
        if (!this.disconnected) {
            this.disconnected = true;
            PacketHandler handler = this.handler;
            PacketHandler finalHandler = handler != null ? handler : this.disconnectHandler;
            if (finalHandler == null) return;

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
}