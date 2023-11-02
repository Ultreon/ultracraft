package com.ultreon.craft.network.packets;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;

public class S2CMenuItemChanged extends Packet<InGameClientPacketHandler> {
    private final int index;
    private final ItemStack stack;

    public S2CMenuItemChanged(int index, ItemStack stack) {
        this.index = index;
        this.stack = stack;
    }

    public S2CMenuItemChanged(PacketBuffer buffer) {
        this.index = buffer.readInt();
        this.stack = buffer.readItemStack();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.index);
        buffer.writeItemStack(this.stack);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onMenuItemChanged(this.index, this.stack);
    }
}
