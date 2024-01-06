package com.ultreon.craft.item;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.InteractResult;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Item {
    private final int maxStackSize;

    public Item(Properties properties) {
        this.maxStackSize = properties.maxStackSize;
    }

    public InteractResult use(UseItemContext useItemContext) {
        return InteractResult.DENY;
    }

    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        ElementID id = this.getId();
        return id == null ? "ultracraft.item.air.name" : id.namespace() + ".item." + id.path() + ".name";
    }

    public ElementID getId() {
        return Registries.ITEM.getKey(this);
    }

    public List<TextObject> getDescription(ItemStack itemStack) {
        return Collections.emptyList();
    }

    public ItemStack defaultStack() {
        return new ItemStack(this);
    }

    /**
     * Get the maximum item stack size.
     *
     * @return the maximum stack size.
     * @see ItemStack#getCount() 
     */
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    /**
     * Item properties.
     */
    public static class Properties {
        private int maxStackSize = 64;

        /**
         * Set the max stack size.
         *
         * @param size the stack size.
         */
        public @This Properties stackSize(int size) {
            this.maxStackSize = size;
            return this;
        }
    }
}
