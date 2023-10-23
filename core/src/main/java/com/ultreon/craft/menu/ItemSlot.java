package com.ultreon.craft.menu;

import com.ultreon.craft.item.ItemStack;

/**
 * Item slot for {@link ContainerMenu}.
 *
 * @see ItemStack
 * @author XyperCode
 */
public class ItemSlot {
    private final ContainerMenu container;
    private ItemStack item;
    private final int slotX;
    private final int slotY;

    public ItemSlot(ContainerMenu container, ItemStack item, int slotX, int slotY) {
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
        this.item = item;
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
}
