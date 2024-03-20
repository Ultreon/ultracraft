package com.ultreon.craft.client.network;

import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.c2s.C2SPingPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Client side connection management.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see Connection
 */
public class ClientConnection implements Runnable {
    private final SocketAddress address;

    /**
     * Creates a new client-side connection
     *
     * @param address the address to connect to
     */
    public ClientConnection(SocketAddress address) {
        this.address = address;
    }

    /**
     * Connects to a local memory server
     *
     * @param address the address to connect to
     * @return the connection
     */
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

    /**
     * Connects to an external server using the provided address.
     *
     * @param inetSocketAddress the address to connect to
     * @param connection        the connection to use
     * @return the channel's future
     */
    public static ChannelFuture connectTo(InetSocketAddress inetSocketAddress, Connection connection) {
        Class<? extends SocketChannel> channelClass;
        Supplier<? extends EventLoopGroup> group;
        if (Epoll.isAvailable()) {
            channelClass = EpollSocketChannel.class;
            group = Connection.NETWORK_EPOLL_WORKER_GROUP;
        } else {
            channelClass = NioSocketChannel.class;
            group = Connection.NETWORK_WORKER_GROUP;
        }

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
            connection.send(new C2SPingPacket());
        }
    }

    /**
     * Starts the connection
     * NOTE: Internal API
     */
    @Override
    @ApiStatus.Internal
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

            ChannelFuture f = b.connect(this.address).sync();

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

        @Override
        protected void initChannel(@NotNull Channel channel) {
            Connection.setInitAttributes(channel);

            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            channel.config().setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(64, 1024, 2097152));

            ChannelPipeline pipeline = channel.pipeline();
            this.connection.setup(pipeline);
            pipeline.addLast("read_timeout", new ReadTimeoutHandler(30));
            this.connection.setupPacketHandler(pipeline);
            this.connection.setHandler(new LoginClientPacketHandlerImpl(this.connection));
        }
    }

    private static class LocalServerInitializer extends ChannelInitializer<Channel> {
        private final Connection connection;

        public LocalServerInitializer(Connection connection) {
            this.connection = connection;
        }

        @Override
        protected void initChannel(@NotNull Channel channel) {
            Connection.setInitAttributes(channel);

            ChannelPipeline pipeline = channel.pipeline();
            this.connection.setup(pipeline);
            this.connection.setupPacketHandler(pipeline);
            this.connection.setHandler(new LoginClientPacketHandlerImpl(this.connection));
        }
    }
}
