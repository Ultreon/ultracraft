package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.server.player.ServerPlayer;

public class C2SChatPacket extends Packet<InGameServerPacketHandler> {
    private final String message;

    public C2SChatPacket(String message) {
        this.message = message;
    }

    public C2SChatPacket(PacketBuffer buffer) {
        this.message = buffer.readUTF(1024);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUTF(this.message, 1024);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        ServerPlayer player = ctx.getPlayer();
        player.onMessageSent(this.message);
    }
}
