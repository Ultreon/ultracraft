package com.ultreon.craft.world.container;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.network.packets.s2c.S2CMenuItemChanged;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jetbrains.annotations.ApiStatus.Internal;
import static org.jetbrains.annotations.ApiStatus.OverrideOnly;

/**
 * An item container that players can open and interact with.
 *
 * @see Player
 * @see ContainerMenu
 */
public abstract class Container implements ContainerView {
    private final Map<ServerPlayer, ContainerMenu> watching = new HashMap<>();
    private final ItemStack[] items = new ItemStack[getContainerSize()];
    private final int containerSize;

    public Container(int containerSize) {
        for (int i = 0; i < containerSize; i++) {
            this.items[i] = ItemStack.empty();
        }

        this.containerSize = containerSize;
        this.onCreated();
    }

    /**
     * Adds a player to the list of watchers.
     *
     * @param player the player to be added
     */
    public void addWatcher(ServerPlayer player, ContainerMenu menu) {
        this.watching.put(player, menu);
    }

    /**
     * Removes a player from the list of watchers.
     *
     * @param player the player to be removed
     */
    public void removeWatcher(ServerPlayer player) {
        this.watching.remove(player);
        if (this.watching.isEmpty()) {
            this.onClosed();
        }
    }

    /**
     * Handles the closing of the container.
     *
     * @param player the player that closed the container.
     */
    @Override
    @Internal
    public final void onContainerClosed(Player player) {
        this.removeWatcher((ServerPlayer) player);
    }

    /**
     * Checks if the container has space for an item.
     *
     * @param item the stack of items to check
     * @return true if the container has space, false otherwise
     */
    @Override
    public boolean hasPlaceFor(ItemStack item) {
        int count = item.getCount();

        for (int i = 0; i < getContainerSize(); i++) {
            if (this.items[i].isEmpty()) {
                return true;
            } else if (this.items[i].sameItemSameData(item)) {
                count -= this.items[i].getCount();
                if (count <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Moves an item into the container.
     *
     * @param item the item to move
     * @return the remainder of the item
     */
    @Override
    public ItemStack moveInto(ItemStack item) {
        for (int i = 0; i < getContainerSize(); i++) {
            if (this.items[i].isEmpty()) {
                this.items[i] = item;
                return ItemStack.empty();
            } else if (this.items[i].sameItemSameData(item)) {
                int remainder = item.transferTo(this.items[i], item.getCount());
                if (remainder == 0) {
                    return ItemStack.empty();
                }
            }
        }

        return item;
    }

    @Override
    public void onItemChanged(int slot, ItemStack newStack) {
        for (var player : this.watching.keySet()) {
            player.connection.send(new S2CMenuItemChanged(slot, newStack));
        }
    }

    /**
     * Handles the creation of the container.
     */
    @OverrideOnly
    protected void onCreated() {

    }

    /**
     * Handles the closing of the container.
     */
    @OverrideOnly
    protected void onClosed() {

    }

    public final int getContainerSize() {
        return this.containerSize;
    }

    public ItemStack getItem(int slot) {
        return this.items[slot];
    }

    public void setItem(int slot, ItemStack item) {
        if (item == null) {
            item = ItemStack.empty();
        }
        this.items[slot] = item;
    }


    public List<ItemStack> moveInto(Iterable<ItemStack> items) {
        List<ItemStack> list = new ArrayList<>();
        for (var item : items) {
            list.add(this.moveInto(item));
        }

        return list;
    }

    public abstract ContainerMenu createMenu(World world, Player player, BlockPos pos);

    @Override
    public void onSlotClick(int slot, Player player, ContainerInteraction interaction) {
//        if (player instanceof ServerPlayer serverPlayer) {
//            this.watching.get(serverPlayer).onSlotClick(slot, serverPlayer, interaction);
//        }
    }
}
