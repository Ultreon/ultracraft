package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CMenuCursorPacket extends Packet<InGameClientPacketHandler> {
    private final ItemStack cursor;

    public S2CMenuCursorPacket(ItemStack cursor) {
        this.cursor = cursor;
    }

    public S2CMenuCursorPacket(PacketBuffer buffer) {
        this.cursor = buffer.readItemStack();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeItemStack(this.cursor);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onMenuCursorChanged(this.cursor);
    }
}
