package com.ultreon.craft.menu;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class ContainerMenu {
    @LazyInit
    @ApiStatus.Internal
    public ItemSlot[] slots;

    private ItemStack cursor = ItemStack.empty();

    public ContainerMenu() {

    }

    public void build() {
        List<ItemSlot> slots = new ArrayList<>();
        this.buildContainer(slots);
        this.slots = slots.toArray(new ItemSlot[0]);
    }

    protected abstract void buildContainer(List<ItemSlot> slots);

    public ItemSlot get(int index) {
        Preconditions.checkElementIndex(index, this.slots.length, "Slot index out of range");
        return this.slots[index];
    }

    public ItemStack getCursor() {
        return this.cursor;
    }

    public void setCursor(ItemStack cursor) {
        Preconditions.checkNotNull(cursor, "cursor");
        this.cursor = cursor;
    }
}
