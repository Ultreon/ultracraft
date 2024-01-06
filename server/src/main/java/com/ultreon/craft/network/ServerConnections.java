package com.ultreon.craft.network;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.s2c.S2CDisconnectPacket;
import com.ultreon.craft.network.packets.s2c.S2CKeepAlivePacket;
import com.ultreon.craft.network.server.LoginServerPacketHandler;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.ElementID;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.function.Supplier;

public class ServerConnections {
    private static final Map<ElementID, NetworkChannel> CHANNELS = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnections.class);
    private final UltracraftServer server;
    private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());

    public static final Supplier<NioEventLoopGroup> SERVER_EVENT_GROUP = Suppliers.memoize(ServerConnections::createServerEventGroup);
    public static final Supplier<EpollEventLoopGroup> SERVER_EPOLL_EVENT_GROUP = Suppliers.memoize(ServerConnections::createEpollEventGroup);
    final List<Connection> connections = new ArrayList<>();
    private boolean running;

    public ServerConnections(UltracraftServer server) {
        this.server = server;
        this.running = true;
    }

    public static NetworkChannel registerChannel(ElementID id) {
        NetworkChannel channel = NetworkChannel.create(id);
        ServerConnections.CHANNELS.put(id, channel);
        return channel;
    }

    public static Collection<NetworkChannel> getChannels() {
        return Collections.unmodifiableCollection(ServerConnections.CHANNELS.values());
    }

    public static NetworkChannel getChannel(ElementID elementID) {
        return ServerConnections.CHANNELS.get(elementID);
    }

    private static NioEventLoopGroup createServerEventGroup() {
        return new NioEventLoopGroup(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1), new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").build());
    }

    private static EpollEventLoopGroup createEpollEventGroup() {
        return new EpollEventLoopGroup(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1), new ThreadFactoryBuilder().setNameFormat("Netty Epoll Server IO #%d").build());
    }

    public SocketAddress startMemoryServer() {
        ChannelFuture channelFuture;

        synchronized (this.channels) {
            channelFuture = new ServerBootstrap()
                    .channel(LocalServerChannel.class)
                    .childHandler(new MemoryChannelInitializer())
                    .group(ServerConnections.SERVER_EVENT_GROUP.get())
                    .localAddress(LocalAddress.ANY)
                    .bind()
                    .syncUninterruptibly();
            this.channels.add(channelFuture);
        }

        return channelFuture.channel().localAddress();
    }

    public void startTcpServer(@Nullable InetAddress address, int port) {
        synchronized (this.channels) {
            Class<? extends ServerChannel> clazz;
            EventLoopGroup group;
            if (Epoll.isAvailable()) {
                clazz = EpollServerSocketChannel.class;
                group = ServerConnections.SERVER_EPOLL_EVENT_GROUP.get();
                ServerConnections.LOGGER.info("Using Epoll server");
            } else {
                clazz = NioServerSocketChannel.class;
                group = ServerConnections.SERVER_EVENT_GROUP.get();
                ServerConnections.LOGGER.info("Using Nio server");
            }

            this.channels.add(new ServerBootstrap()
                    .channel(clazz)
                    .childHandler(new TcpChannelInitializer())
                    .group(group)
                    .localAddress(address, port)
                    .bind()
                    .syncUninterruptibly());
        }
    }

    public void tick() {
        synchronized (this.connections) {
            Iterator<Connection> iterator = this.connections.iterator();

            while (true) {
                Connection connection;
                do {
                    if (!iterator.hasNext()) {
                        return;
                    }

                    connection = iterator.next();
                } while (connection.isConnecting());

                if (connection.isConnected()) {
                    try {
                        if (connection.tickKeepAlive()) {
                            connection.send(new S2CKeepAlivePacket());
                        }
                        connection.tick();
                    } catch (Exception e) {
                        if (connection.isMemoryConnection()) {
                            this.server.crash(new RuntimeException("Failed to tick packet", e));
                        }

                        ServerConnections.LOGGER.warn("Failed to handle packet:", e);
                        Connection finalConnection = connection;
                        String message = "Server failed to tick the connection";
                        connection.send(new S2CDisconnectPacket<>(message), PacketResult.onEither(() -> finalConnection.disconnect(message)));
                        connection.setReadOnly();
                    }
                } else {
                    iterator.remove();
                    connection.handleDisconnect();
                }
            }
        }
    }

    public UltracraftServer getServer() {
        return this.server;
    }

    public void stop() {
        this.running = false;

        for (ChannelFuture future : this.channels) {
            try {
                future.channel().close().sync();
                ServerConnections.SERVER_EVENT_GROUP.get().shutdownGracefully().sync();
            } catch (InterruptedException ex) {
                ServerConnections.LOGGER.warn("Failed to close channel", ex);
            }
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public String toString() {
        return "ServerConnections{}";
    }

    private class TcpChannelInitializer extends ChannelInitializer<Channel> {
        @Override
        protected void initChannel(@NotNull Channel channel) {
            Connection.setInitAttributes(channel);

            try {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException ignored) {

            }

            ChannelPipeline pipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            Connection connection = new Connection(PacketDestination.CLIENT);
            ServerConnections.this.connections.add(connection);
            connection.setup(pipeline);
            connection.setupPacketHandler(pipeline);
            connection.setHandler(new LoginServerPacketHandler(ServerConnections.this.server, connection));
        }
    }

    private class MemoryChannelInitializer extends ChannelInitializer<Channel> {
        @Override
        protected void initChannel(@NotNull Channel channel) {
            Connection.setInitAttributes(channel);

            Connection connection = new Connection(PacketDestination.CLIENT);
            connection.memoryConnection = true;
            ServerConnections.this.connections.add(connection);
            ChannelPipeline channelPipeline = channel.pipeline();
            connection.setup(channelPipeline);
            connection.setupPacketHandler(channelPipeline);
            connection.setHandler(new LoginServerPacketHandler(ServerConnections.this.server, connection));
        }
    }
}
