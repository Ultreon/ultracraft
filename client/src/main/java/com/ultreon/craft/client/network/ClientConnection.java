package com.ultreon.craft.client.network;

import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.s2c.S2CKeepAlivePacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class ClientConnection implements Runnable {
    private final String host;
    private final int port;

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static Connection connectToLocalServer(SocketAddress address) {
        Connection connection = new Connection(PacketDestination.SERVER);
        new Bootstrap()
                .group(Connection.LOCAL_WORKER_GROUP.get())
                .handler(new LocalServerInitializer(connection))
                .channel(LocalChannel.class)
                .connect(address)
                .syncUninterruptibly();
        return connection;
    }

    public static ChannelFuture connectTo(InetSocketAddress inetSocketAddress, Connection connection) {
        Class<? extends SocketChannel> channelClass;
        Supplier<? extends EventLoopGroup> group;
        channelClass = NioSocketChannel.class;
        group = Connection.NETWORK_WORKER_GROUP;

        connection.setGroup(group.get());

        return new Bootstrap()
                .group(group.get())
                .handler(new MultiplayerChannelInitializer(connection))
                .channel(channelClass)
                .connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
    }

    public static Future<?> closeGroup() {
        return Connection.NETWORK_WORKER_GROUP.get().shutdownGracefully();
    }

    public void tick(Connection connection) {
        if (connection.tickKeepAlive()) {
            connection.send(new S2CKeepAlivePacket());
        }
    }

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(@NotNull SocketChannel ch) {
                    Connection connection = new Connection(PacketDestination.SERVER);
                    connection.setup(ch.pipeline());
                    connection.setHandler(new LoginClientPacketHandlerImpl(connection));
                }
            });

            ChannelFuture f = b.connect(this.host, this.port).sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static class MultiplayerChannelInitializer extends ChannelInitializer<Channel> {
        private final Connection connection;

        public MultiplayerChannelInitializer(Connection connection) {
            this.connection = connection;
        }

        protected void initChannel(@NotNull Channel channel) {
            Connection.setInitAttributes(channel);

            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            channel.config().setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(64, 1024, 2097152));

            ChannelPipeline pipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            this.connection.setup(pipeline);
            this.connection.setupPacketHandler(pipeline);
            this.connection.setHandler(new LoginClientPacketHandlerImpl(this.connection));
        }
    }

    private static class LocalServerInitializer extends ChannelInitializer<Channel> {
        private final Connection connection;

        public LocalServerInitializer(Connection connection) {
            this.connection = connection;
        }

        protected void initChannel(@NotNull Channel channel) {
            Connection.setInitAttributes(channel);

            ChannelPipeline pipeline = channel.pipeline();
            this.connection.setup(pipeline);
            this.connection.setupPacketHandler(pipeline);
            this.connection.setHandler(new LoginClientPacketHandlerImpl(this.connection));
        }
    }
}
