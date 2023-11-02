package com.ultreon.craft.network.packets.ingame;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CKeepAlivePacket<T extends PacketHandler> extends Packet<T> {
    public S2CKeepAlivePacket() {

    }

    public S2CKeepAlivePacket(PacketBuffer ignoredBuffer) {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public void handle(PacketContext ctx, PacketHandler handler) {

    }
}
