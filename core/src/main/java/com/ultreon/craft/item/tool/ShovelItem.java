package com.ultreon.craft.item.tool;

import com.ultreon.craft.item.material.ItemMaterial;
import com.ultreon.craft.render.UV;

public class ShovelItem extends ToolItem {
    public ShovelItem(UV textureUV, ItemMaterial material) {
        super(textureUV, material);
    }

    @Override
    public ToolType getToolType() {
        return ToolType.SHOVEL;
    }
}
