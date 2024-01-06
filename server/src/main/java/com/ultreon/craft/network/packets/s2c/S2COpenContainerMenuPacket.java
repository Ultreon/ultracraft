package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.util.ElementID;

public class S2COpenContainerMenuPacket extends Packet<InGameClientPacketHandler> {
    private final ElementID menuType;

    public S2COpenContainerMenuPacket(ElementID menuType) {
        this.menuType = menuType;
    }

    public S2COpenContainerMenuPacket(PacketBuffer buffer) {
        this.menuType = buffer.readId();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeId(this.menuType);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onOpenContainerMenu(this.menuType);
    }
}
