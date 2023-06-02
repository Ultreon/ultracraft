package com.ultreon.craft.item.tool;

import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.material.ItemMaterial;
import com.ultreon.craft.render.UV;

public abstract class ToolItem extends Item {
    private final ItemMaterial material;

    public ToolItem(UV textureUV, ItemMaterial material) {
        super(textureUV);
        this.material = material;
    }

    public abstract ToolType getToolType();

    public float getEfficiency() {
        return material.getEfficiency();
    }
}
