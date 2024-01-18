package com.ultreon.craft.menu;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CInventoryItemChangedPacket;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Inventory extends ContainerMenu {
    public final ItemSlot[] hotbar = new ItemSlot[9];
    public final ItemSlot[][] inv = new ItemSlot[9][3];

    private final Player holder;

    public Inventory(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockPos pos) {
        super(type, world, entity, pos, 36);

        if (!(entity instanceof Player player)) {
            throw new IllegalArgumentException("Entity must be a player!");
        }

        this.holder = player;
        this.addWatcher(this.holder);
    }

    @Override
    public void build() {
        int idx = 0;
        for (int x = 0; x < 9; x++) {
            this.hotbar[x] = this.addSlot(new ItemSlot(idx++, this, new ItemStack(), x * 19 + 6, 83));
        }

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                this.inv[x][y] = this.addSlot(new ItemSlot(idx++, this, new ItemStack(), x * 19 + 6, y * 19 + 6));
            }
        }
    }

    @Override
    protected @Nullable Packet<InGameClientPacketHandler> createPacket(ServerPlayer player, ItemSlot slot) {
        if (this.holder != player) return null;

        return new S2CInventoryItemChangedPacket(slot.index, slot.getItem());
    }

    public ItemSlot getHotbarSlot(int index) {
        Preconditions.checkElementIndex(index, this.hotbar.length, "Invalid hotbar index");
        return this.hotbar[index];
    }

    public List<ItemSlot> getHotbarSlots() {
        return List.of(this.hotbar);
    }

    /**
     * Adds a list of item stacks to the inventory.
     *
     * @param stacks the list of item stacks.
     * @return true if all items could fit.
     */
    @CanIgnoreReturnValue
    public boolean addItems(Iterable<ItemStack> stacks) {
        boolean fit = true;
        for (ItemStack stack : stacks) {
            fit &= this.addItem(stack.copy());
        }
        return fit;
    }

    /**
     * Adds an item stack to the inventory holder.
     *
     * @param stack the item stack to add.
     * @return true if the item stack could fully fit in the inventory.
     */
    @CanIgnoreReturnValue
    public boolean addItem(ItemStack stack) {
        if (this.getWorld().isClientSide()) return false; // Ignore client side inventory.

        for (ItemSlot slot : this.slots) {
            ItemStack slotItem = slot.getItem();

            if (slotItem.isEmpty()) {
                int maxStackSize = stack.getItem().getMaxStackSize();
                int transferAmount = Math.min(stack.getCount(), maxStackSize);
                stack.transferTo(slotItem, transferAmount);
                this.onItemChanged(slot);
            } else if (slotItem.sameItemSameData(stack)) {
                stack.transferTo(slotItem, stack.getCount());
                this.onItemChanged(slot);
            }

            // If the stack is fully distributed, exit the loop
            if (stack.isEmpty()) {
                return true;
            }
        }

        // If the loop completes and there's still some stack remaining, it means it couldn't be fully added to slots.
        return stack.isEmpty();
    }

    public Player getHolder() {
        return this.holder;
    }
}
