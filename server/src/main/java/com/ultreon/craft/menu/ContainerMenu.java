package com.ultreon.craft.menu;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.MenuEvents;
import com.ultreon.craft.events.api.EventResult;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CMenuItemChanged;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class that holds a bunch of item slots.
 *
 * @see ItemSlot
 * @see ItemStack
 * @see MenuType
 */
public abstract class ContainerMenu {
    private final @NotNull MenuType<?> type;
    private final @NotNull World world;
    private final @NotNull Entity entity;
    private final @Nullable BlockPos pos;
    @LazyInit
    @ApiStatus.Internal
    public ItemSlot[] slots;

    protected final List<Player> watching = new CopyOnWriteArrayList<>();
    private @Nullable TextObject customTitle = null;

    /**
     * Creates a new {@link ContainerMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param pos    the position where the menu is opened.
     * @param size   the number of slots.
     */
    protected ContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockPos pos, int size) {
        Preconditions.checkNotNull(type, "Menu type cannot be null!");
        Preconditions.checkNotNull(world, "World cannot be null!");
        Preconditions.checkNotNull(entity, "Entity cannot be null!");
        Preconditions.checkArgument(size >= 0, "Size cannot be negative!");

        this.type = type;
        this.world = world;
        this.entity = entity;
        this.pos = pos;
        this.slots = new ItemSlot[size];
    }

    protected final ItemSlot addSlot(ItemSlot slot) {
        this.slots[slot.index] = slot;
        return slot;
    }

    public @NotNull MenuType<?> getType() {
        return this.type;
    }

    public @NotNull World getWorld() {
        return this.world;
    }

    public @NotNull Entity getEntity() {
        return this.entity;
    }

    public @Nullable BlockPos getPos() {
        return this.pos;
    }

    public abstract void build();

    public ItemSlot get(int index) {
        Preconditions.checkElementIndex(index, this.slots.length, "Slot index out of chance");
        return this.slots[index];
    }

    protected void onItemChanged(ItemSlot slot) {
        for (Player player : this.watching) {
            if (player instanceof ServerPlayer serverPlayer) {
                Packet<InGameClientPacketHandler> packet = this.createPacket(serverPlayer, slot);
                if (packet != null) {
                    serverPlayer.connection.send(packet);
                }
            }
        }
    }

    protected @Nullable Packet<InGameClientPacketHandler> createPacket(ServerPlayer player, ItemSlot slot) {
        if (player.getOpenMenu() != this) return null;

        return new S2CMenuItemChanged(slot.index, slot.getItem());
    }

    @CanIgnoreReturnValue
    public ItemStack setItem(int index, ItemStack stack) {
        return this.slots[index].setItem(stack, false);
    }

    public ItemStack getItem(int index) {
        return this.slots[index].getItem();
    }

    public ItemStack takeItem(int index) {
        return this.slots[index].takeItem();
    }

    /**
     * Adds a player to the list of watchers.
     *
     * @param player the player to be added
     */
    public void addWatcher(Player player) {
        this.watching.add(player);
    }

    /**
     * Removes a player from the list of watchers.
     *
     * @param player the player to be removed
     */
    public void removeWatcher(Player player) {
        if (!this.watching.contains(player)) {
            UltracraftServer.LOGGER.warn("Player {} is not a watcher of {}", player, this);
            return;
        }
        this.watching.remove(player);
        if (this.watching.isEmpty()) {
            this.close();
        }
    }

    private void close() {
        this.world.closeMenu(this);
    }

    /**
     * onTakeItem method is called when a player takes an item from a specific slot.
     * <p>NOTE: This method is meant for override only</p>
     *
     * @param  player      the server player who is taking the item
     * @param  index       the index of the slot from which the item is being taken
     * @param  rightClick  a boolean indicating whether the player right-clicked to take the item
     */
    @ApiStatus.OverrideOnly
    public void onTakeItem(ServerPlayer player, int index, boolean rightClick) {
        ItemSlot slot = this.slots[index];

        EventResult result = MenuEvents.MENU_CLICK.factory().onMenuClick(this, player, slot, rightClick);
        if (result.isCanceled())
            return;

        if (rightClick) {
            // Right click transfer
            if (player.getCursor().isEmpty()) {
                // Split item from slot and put it in the cursor
                ItemStack item = slot.getItem().split();
                player.setCursor(item);
            } else {
                // Transfer one item from cursor to slot
                player.getCursor().transferTo(slot.getItem());
                player.setCursor(player.getCursor());
            }
            return;
        }

        // Left click transfer
        ItemStack cursor = player.getCursor();
        ItemStack slotItem = slot.getItem();

        if (!cursor.isEmpty() && cursor.sameItemSameData(slotItem)) {
            // Take item from cursor and put it in the slot, remaining items are left in the cursor.
            cursor.transferTo(slotItem, cursor.getCount());
            player.setCursor(player.getCursor());
            return;
        }

        if (cursor.isEmpty()) {
            // Take item from slot and put it in the cursor
            player.setCursor(slot.takeItem());
        } else {
            // Swap items between cursor and slot
            slot.setItem(cursor);
            cursor = slotItem;

            player.setCursor(cursor);
        }
    }

    /**
     * Retrieves the title of the menu.
     *
     * @return the title
     */
    public TextObject getTitle() {
        Identifier id = this.getType().getId();

        if (this.customTitle == null)
            return TextObject.translation(id.namespace() + ".container." + id.path().replace("/", ".") + ".title");
        return this.customTitle;
    }

    /**
     * Gets the custom title of the menu.
     *
     * @return the custom title or null if it isn't set.
     */
    public @Nullable TextObject getCustomTitle() {
        return this.customTitle;
    }

    /**
     * Sets the custom title of the menu.
     *
     * @param customTitle the custom title to set or null to remove it.
     */
    public void setCustomTitle(@Nullable TextObject customTitle) {
        this.customTitle = customTitle;
    }

    @Override
    public String toString() {
        return "ContainerMenu[" + this.getType().getId() + "]";
    }
}
