package com.ultreon.craft.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.compression.CompressionDecoder;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.stage.PacketStages;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.compression.*;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import net.fabricmc.api.EnvType;
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

    private final PacketDestination direction;
    @Nullable
    private Channel channel;
    @Nullable
    private SocketAddress remoteAddress;
    private final Queue<Runnable> tasks = Queues.newArrayDeque();
    private String disconnectMsg;

    public static final Supplier<NioEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(Connection::createLocalWorkerGroup);
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
    private ExecutorService dispatchExecutor = Executors.newFixedThreadPool(8);

    public Connection(PacketDestination direction) {
        this.direction = direction;
        this.context = new PacketContext(null, this, direction.getSourceEnv());
    }

    private static NioEventLoopGroup createLocalWorkerGroup() {
        return new NioEventLoopGroup(8, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
    }

    private static NioEventLoopGroup createNetworkWorkerGroup() {
        return new NioEventLoopGroup(8, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
    }

    public static int getPacketsSent() {
        return packetsSent;
    }

    public static int getPacketsReceived() {
        return packetsReceived;
    }

    public void delayDisconnect(String message) {
        this.disconnectMsg = message;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        this.channel = ctx.channel();
        this.remoteAddress = this.channel.remoteAddress();

        String msg = this.direction.getSourceEnv() == EnvType.CLIENT ? "Connected to: " : "Connected by: ";
        Connection.LOGGER.info(msg + (this.remoteAddress != null ? this.remoteAddress.toString() : null));

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
        boolean bl = !this.handlingFault;
        this.handlingFault = true;
        if (this.channel != null && this.channel.isOpen()) {
            if (cause instanceof TimeoutException) {
                Connection.LOGGER.debug("Timeout", cause);
                this.disconnect("Timed Out");
            } else {
                var message = "Internal Exception: " + cause;
                if (bl) {
                    Connection.LOGGER.debug("Failed to send packet", cause);
                    this.disconnect(message);
                    this.setReadOnly();
                } else {
                    Connection.LOGGER.debug("Double fault", cause);
                    this.disconnect(message);
                }
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

    public void disconnect(@NotNull String message) {
        if (this.channel == null || !this.channel.isOpen()) return;

        @NotNull String msg = this.direction.getSourceEnv() == EnvType.CLIENT ? "Disconnected by: " : "Disconnected: ";
        Connection.LOGGER.info(msg + (this.remoteAddress != null ? this.remoteAddress.toString() : null));

        this.handler.onDisconnect(message);
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
        if (this.channel == null) return;
        if (!this.channel.isOpen()) throw new ClosedChannelException();

        Connection.packetsSent++;

        if (this.channel.eventLoop().inEventLoop()) {
            this._sendInEventLoop(packet, stateListener, flush);
        } else {
            this.channel.eventLoop().execute(() -> this._sendInEventLoop(packet, stateListener, flush));
        }
    }

    private void _sendInEventLoop(Packet<?> packet, @Nullable PacketResult stateListener, boolean flush) {
        try {
            Preconditions.checkNotNull(packet, "packet");
            if (this.channel == null) return;
            if (!this.channel.isOpen()) throw new ClosedChannelException();

            ChannelFuture sent = flush ? this.channel.writeAndFlush(packet) : this.channel.write(packet);
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
                        Connection.LOGGER.error("Failed to handle response: " + packet.getClass().getName());
                    }
                });
            }
        } catch (Exception e) {
            Connection.LOGGER.error("Failed to sent packet: " + packet.getClass().getName());
        }
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) {
        if (this.channel != null && this.channel.isOpen()) {
            PacketHandler handler = this.handler;
            if (handler == null) {
                throw new IllegalStateException("Packet handler isn't set yet!");
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

        channel.config().setAllocator(new UnpooledByteBufAllocator(false, true));
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

            pipeline.addLast("encoder", new PacketEncoder(ourData));
            pipeline.addLast("decoder", new PacketDecoder(theirData));
            pipeline.addLast("decompress", new JZlibDecoder());
            pipeline.addLast("compress", new JZlibEncoder(9));
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
        if (this.channel != null && !this.channel.isOpen() && !this.disconnected) {
            this.disconnected = true;
            PacketHandler handler = this.handler;
            PacketHandler finalHandler = handler != null ? handler : this.disconnectHandler;
            if (finalHandler == null) return;

            String message = this.disconnectMsg;
            if (message == null) message = "Connection lost";

            finalHandler.onDisconnect(message);

        }
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }
}