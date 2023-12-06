package com.ultreon.craft.server.player;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

import java.util.List;

public class S2CCommandSyncPacket extends Packet<InGameClientPacketHandler> {
    private final List<String> commands;

    public S2CCommandSyncPacket(List<String> commands) {
        this.commands = commands;
    }

    public S2CCommandSyncPacket(PacketBuffer buffer) {
        this.commands = buffer.readList(buf -> buf.readUTF(64));
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeList(this.commands, (buf, s) -> buf.writeUTF(s, 64));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {

    }
}
