package com.ultreon.craft.item.tool;

import com.ultreon.craft.item.material.ItemMaterial;

public class ShovelItem extends ToolItem {
    public ShovelItem(Properties textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.SHOVEL;
    }
}
