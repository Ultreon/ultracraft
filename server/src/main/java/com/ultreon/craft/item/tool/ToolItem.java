package com.ultreon.craft.item.tool;

import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.material.ItemMaterial;

public abstract class ToolItem extends Item {
    private final ItemMaterial material;

    public ToolItem(Properties properties, ItemMaterial material) {
        super(properties);
        this.material = material;
    }

    public abstract ToolType getToolType();

    public float getEfficiency() {
        return this.material.getEfficiency();
    }
}
