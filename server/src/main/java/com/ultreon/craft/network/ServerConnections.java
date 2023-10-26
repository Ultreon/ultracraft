package com.ultreon.craft.network;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.s2c.S2CDisconnectPacket;
import com.ultreon.craft.network.server.LoginServerPacketHandler;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.libs.commons.v0.Identifier;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
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
    private static final Map<Identifier, NetworkChannel> CHANNELS = new HashMap<>();
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

    public static NetworkChannel registerChannel(Identifier id) {
        NetworkChannel channel = NetworkChannel.create(id);
        ServerConnections.CHANNELS.put(id, channel);
        return channel;
    }

    public static Collection<NetworkChannel> getChannels() {
        return Collections.unmodifiableCollection(ServerConnections.CHANNELS.values());
    }

    public static NetworkChannel getChannel(Identifier identifier) {
        return ServerConnections.CHANNELS.get(identifier);
    }

    private static NioEventLoopGroup createServerEventGroup() {
        return new NioEventLoopGroup(8, new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").setDaemon(true).build());
    }

    private static EpollEventLoopGroup createEpollEventGroup() {
        return new EpollEventLoopGroup(8, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
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
                ServerConnections.LOGGER.info("Using epoll channel type");
            } else {
                clazz = NioServerSocketChannel.class;
                group = ServerConnections.SERVER_EVENT_GROUP.get();
                ServerConnections.LOGGER.info("Using default channel type");
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
        synchronized(this.connections) {
            Iterator<Connection> iterator = this.connections.iterator();

            while(true) {
                Connection connection;
                do {
                    if (!iterator.hasNext()) {
                        return;
                    }

                    connection = iterator.next();
                } while(connection.isConnecting());

                if (connection.isConnected()) {
                    try {
                        connection.tick();
                    } catch (Exception var7) {
                        if (connection.isMemoryConnection()) {
                            this.server.crash(new RuntimeException("Ticking memory connection"));
                        }

                        ServerConnections.LOGGER.warn("Failed to handle packet:", var7);
                        Connection finalConnection = connection;
                        connection.send(new S2CDisconnectPacket("Internal server error"), PacketResult.onEither(() -> {
                            finalConnection.disconnect("Internal server error");
                        }));
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
            } catch (InterruptedException var4) {
                ServerConnections.LOGGER.error("Interrupted whilst closing channel");
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
        protected void initChannel(@NotNull Channel channel) {
            Connection.setInitAttributes(channel);

            try {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException ignored) {

            }

            ChannelPipeline pipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            Connection connection = new Connection(PacketDestination.SERVER);
            ServerConnections.this.connections.add(connection);
            connection.setup(pipeline);
            connection.setupPacketHandler(pipeline);
            connection.setHandler(new LoginServerPacketHandler(ServerConnections.this.server, connection));
        }
    }

    private class MemoryChannelInitializer extends ChannelInitializer<Channel> {
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
