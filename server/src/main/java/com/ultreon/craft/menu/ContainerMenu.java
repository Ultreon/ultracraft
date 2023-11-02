package com.ultreon.craft.menu;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.network.packets.S2CMenuItemChanged;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ContainerMenu {
    private final MenuType<?> type;
    private final World world;
    private final Entity entity;
    @Nullable private final BlockPos pos;
    @LazyInit
    @ApiStatus.Internal
    public ItemSlot[] slots;

    private final List<Player> players = new ArrayList<>();

    public ContainerMenu(MenuType<?> type, World world, Entity entity, @Nullable BlockPos pos) {
        this.type = type;
        this.world = world;
        this.entity = entity;
        this.pos = pos;
    }

    public MenuType<?> getType() {
        return this.type;
    }

    public World getWorld() {
        return this.world;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public @Nullable BlockPos getPos() {
        return this.pos;
    }

    public final void build() {
        List<ItemSlot> slots = new ArrayList<>();
        this.buildContainer(slots);

        this.slots = new ItemSlot[slots.size()];
        for (int idx = 0, slotsSize = slots.size(); idx < slotsSize; idx++) {
            ItemSlot slot = slots.get(idx);
            slot.index = idx;
            this.slots[idx] = slot;
        }
    }

    protected abstract void buildContainer(List<ItemSlot> slots);

    public ItemSlot get(int index) {
        Preconditions.checkElementIndex(index, this.slots.length, "Slot index out of range");
        return this.slots[index];
    }

    protected void onItemChanged(ItemSlot slot) {
        if (this.world instanceof ServerWorld serverWorld) {
            for (ServerPlayer player : serverWorld.getServer().getPlayers()) {
                if (player.getOpenMenu() != this) continue;

                player.connection.send(new S2CMenuItemChanged(slot.index, slot.getItem()));
            }
        }
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

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        if (this.players.isEmpty()) {
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
