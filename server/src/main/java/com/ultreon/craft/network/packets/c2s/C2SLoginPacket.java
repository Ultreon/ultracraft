package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.LoginServerPacketHandler;

public class C2SLoginPacket extends Packet<LoginServerPacketHandler> {
    private final String name;

    public C2SLoginPacket(String name) {
        this.name = name;
    }

    public C2SLoginPacket(PacketBuffer buffer) {
        this.name = buffer.readUTF(20);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUTF(this.name, 20);
    }

    @Override
    public void handle(PacketContext ctx, LoginServerPacketHandler handler) {
        handler.onPlayerLogin(this.name);
    }
}
