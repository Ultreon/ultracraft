package com.ultreon.craft.menu;

import com.ultreon.craft.item.ItemStack;

public class RedirectItemSlot extends ItemSlot {
    private final ItemSlot target;

    public RedirectItemSlot(int index, ItemSlot target, int slotX, int slotY) {
        super(index, target.getContainer(), target.getItem(), slotX, slotY);

        this.target = target;
    }

    @Override
    public ItemStack getItem() {
        return this.target.getItem();
    }

    @Override
    public void setItem(ItemStack item) {
        this.target.setItem(item);
    }

    @Override
    public ItemStack setItem(ItemStack item, boolean emitEvent) {
        return this.target.setItem(item, emitEvent);
    }

    @Override
    public ContainerMenu getContainer() {
        return this.target.getContainer();
    }

    @Override
    public ItemStack takeItem() {
        return this.target.takeItem();
    }

    @Override
    public String toString() {
        return this.target.toString();
    }

    @Override
    public ItemStack split() {
        return this.target.split();
    }

    @Override
    public ItemStack split(int amount) {
        return this.target.split(amount);
    }

    @Override
    public void update() {
        this.target.update();
    }

    @Override
    public boolean isEmpty() {
        return this.target.isEmpty();
    }

    @Override
    public void shrink(int amount) {
        this.target.shrink(amount);
    }

    @Override
    public void grow(int amount) {
        this.target.grow(amount);
    }
}
