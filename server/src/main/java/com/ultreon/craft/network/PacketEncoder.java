package com.ultreon.craft.network;

import com.ultreon.craft.network.packets.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private final AttributeKey<? extends PacketData<?>> ourDataKey;

    public PacketEncoder(AttributeKey<? extends PacketData<?>> ourDataKey) {
        this.ourDataKey = ourDataKey;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) {
        PacketBuffer buffer = new PacketBuffer(out);
        PacketData<?> data = ctx.channel().attr(this.ourDataKey).get();
        buffer.writeInt((data).getId(msg));
        data.encode(msg, buffer);
    }

}