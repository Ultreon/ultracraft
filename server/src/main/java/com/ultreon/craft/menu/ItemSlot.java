package com.ultreon.craft.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.item.ItemStack;

/**
 * Item slot for {@link ContainerMenu}.
 *
 * @see ItemStack
 * @author XyperCode
 */
public class ItemSlot {
    private final ContainerMenu container;
    int index;
    private ItemStack item;
    private final int slotX;
    private final int slotY;

    public ItemSlot(int index, ContainerMenu container, ItemStack item, int slotX, int slotY) {
        this.index = index;
        this.container = container;
        this.item = item;
        this.slotX = slotX;
        this.slotY = slotY;
    }

    /**
     * @return the item in the slot.
     */
    public ItemStack getItem() {
        return this.item;
    }

    /**
     * @param item the item to put in the slot.
     */
    public void setItem(ItemStack item) {
        this.setItem(item, true);
    }

    /**
     * @param item the item to put in the slot.
     * @return the previous item in the slot.
     */
    @CanIgnoreReturnValue
    public ItemStack setItem(ItemStack item, boolean emitEvent) {
        ItemStack old = this.item;
        this.item = item;

        if (emitEvent) this.container.onItemChanged(this);
        return old;
    }

    /**
     * @return the slot's x coordinate in the GUI.
     */
    public int getSlotX() {
        return this.slotX;
    }

    /**
     * @return the slot's y coordinate in the GUI.
     */
    public int getSlotY() {
        return this.slotY;
    }

    /**
     * @return the container menu the slot it in.
     */
    public ContainerMenu getContainer() {
        return this.container;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= this.getSlotX() && y >= this.getSlotY() && x <= this.getSlotX() + 16 && y <= this.getSlotY() + 16;
    }

    public int getIndex() {
        return this.index;
    }

    /**
     * Takes an item from the slot. This will set the current item to empty and return the original item.
     *
     * @return the item in the slot.
     */
    public ItemStack takeItem() {
        ItemStack copy = this.item;
        this.item = new ItemStack();
        return copy;
    }

    @Override
    public String toString() {
        return "ItemSlot(" + this.index + ')';
    }
}
