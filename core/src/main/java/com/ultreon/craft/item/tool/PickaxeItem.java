package com.ultreon.craft.item.tool;

import com.ultreon.craft.item.material.ItemMaterial;

public class PickaxeItem extends ToolItem {
    public PickaxeItem(Properties textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.PICKAXE;
    }
}
