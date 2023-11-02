package com.ultreon.craft.network.packets.ingame;

import com.ultreon.craft.network.*;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.libs.commons.v0.Identifier;

public class S2CModPacket extends Packet<InGameClientPacketHandler> {
    private final Identifier channelId;
    private final ModPacket<?> packet;
    private final NetworkChannel channel;

    public S2CModPacket(NetworkChannel channel, ModPacket<?> packet) {
        this.channel = channel;
        this.channelId = channel.id();
        this.packet = packet;
    }

    public S2CModPacket(PacketBuffer buffer) {
        this.channelId = buffer.readId();
        this.channel = NetworkChannel.getChannel(this.channelId);
        this.packet = this.channel.getDecoder(buffer.readUnsignedShort()).apply(buffer);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeId(this.channelId);
        buffer.writeShort(this.channel.getId(this.packet));

        this.packet.toBytes(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onModPacket(this.channel, this.packet);
    }
}
