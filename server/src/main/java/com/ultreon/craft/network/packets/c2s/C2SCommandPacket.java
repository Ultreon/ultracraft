package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.server.player.ServerPlayer;

public class C2SCommandPacket extends Packet<InGameServerPacketHandler> {
    private final String input;

    public C2SCommandPacket(String input) {
        this.input = input;
    }

    public C2SCommandPacket(PacketBuffer buffer) {
        this.input = buffer.readString(32768);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUTF(this.input, 32768);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        ServerPlayer player = ctx.getPlayer();
        if (player != null) {
            player.execute(this.input);
        }
    }
}
