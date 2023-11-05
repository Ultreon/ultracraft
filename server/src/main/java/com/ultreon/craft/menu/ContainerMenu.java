package com.ultreon.craft.menu;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CMenuItemChanged;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ContainerMenu {
    private final @NotNull MenuType<?> type;
    private final @NotNull World world;
    private final @NotNull Entity entity;
    private final @Nullable BlockPos pos;
    @LazyInit
    @ApiStatus.Internal
    public ItemSlot[] slots;

    protected final List<Player> watching = new ArrayList<>();

    public ContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockPos pos, int size) {
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
        Preconditions.checkElementIndex(index, this.slots.length, "Slot index out of range");
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

    public void addWatcher(Player player) {
        this.watching.add(player);
    }

    public void removePlayer(Player player) {
        this.watching.remove(player);
        if (this.watching.isEmpty()) {
            this.close();
        }
    }

    private void close() {
        this.world.closeMenu(this);
    }

    public void onTakeItem(ServerPlayer player, int index, boolean split) {
        ItemSlot slot = this.slots[index];
        ItemStack item;
        if (split && slot.getItem().getCount() > 1) {
            item = slot.getItem().split();
        } else {
            item = slot.takeItem();
        }

        player.setCursor(item);
    }
}
