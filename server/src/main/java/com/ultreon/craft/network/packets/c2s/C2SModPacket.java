package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.util.ElementID;

public class C2SModPacket extends Packet<InGameServerPacketHandler> {
    private final ElementID channelId;
    private final ModPacket<?> packet;
    private NetworkChannel channel;

    public C2SModPacket(NetworkChannel channel, ModPacket<?> packet) {
        this.channel = channel;
        this.channelId = channel.id();
        this.packet = packet;
    }

    public C2SModPacket(PacketBuffer buffer) {
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
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onModPacket(this.channel, this.packet);
    }

    public NetworkChannel getChannel() {
        return this.channel;
    }
}
