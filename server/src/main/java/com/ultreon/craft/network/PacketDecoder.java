package com.ultreon.craft.network;

import com.ultreon.craft.network.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    private final AttributeKey<? extends PacketData<?>> theirDataKey;

    /**
     * @param theirDataKey the key of the attribute that holds the packet data
     */
    public PacketDecoder(AttributeKey<? extends PacketData<?>> theirDataKey) {
        this.theirDataKey = theirDataKey;
    }

    /**
     * Reads a packet from a {@link ByteBuf}, and puts it into {@code out} parameter.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @param out the {@link List} to which decoded messages should be added
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        PacketBuffer buffer = new PacketBuffer(in);
        int id = buffer.readInt();

        PacketData<?> data = ctx.channel().attr(this.theirDataKey).get();
        Packet<?> packet = data.decode(id, buffer);
        out.add(packet);
    }
}