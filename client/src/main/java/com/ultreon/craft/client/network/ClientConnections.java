package com.ultreon.craft.client.network;

import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.api.PacketDestination;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.Bzip2Encoder;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateClientExtensionHandshaker;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.ultreon.craft.network.Connection.NETWORK_WORKER_GROUP;

public class ClientConnections implements Runnable {

    private final String host;
    private final int port;

    public ClientConnections(String host, int port) {
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
        return new Bootstrap()
                .group(Connection.NETWORK_WORKER_GROUP.get())
                .handler(new MultiplayerChannelInitializer(connection))
                .channel(NioSocketChannel.class)
                .connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
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

            ChannelPipeline pipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            this.connection.setup(pipeline);
            this.connection.setupPacketHandler(pipeline);
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
