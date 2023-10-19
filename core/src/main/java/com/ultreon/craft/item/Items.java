package com.ultreon.craft.item;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.item.material.ItemMaterials;
import com.ultreon.craft.item.tool.PickaxeItem;
import com.ultreon.craft.item.tool.ShovelItem;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.UV;
import com.ultreon.libs.commons.v0.Identifier;

public class Items {
    public static final Item AIR = Items.register("air", new Item(null));
    public static final BlockItem GRASS_BLOCK = Items.register("grass_block", new BlockItem(Blocks.GRASS_BLOCK));
    public static final BlockItem DIRT = Items.register("dirt", new BlockItem(Blocks.DIRT));
    public static final BlockItem SAND = Items.register("sand", new BlockItem(Blocks.SAND));
    public static final BlockItem STONE = Items.register("stone", new BlockItem(Blocks.STONE));
    public static final BlockItem COBBLESTONE = Items.register("cobblestone", new BlockItem(Blocks.COBBLESTONE));
    public static final BlockItem WATER = Items.register("water", new BlockItem(Blocks.WATER));
    public static final PickaxeItem WOODEN_PICKAXE = Items.register("wooden_pickaxe", new PickaxeItem(new UV(0, 0), ItemMaterials.WOOD));
    public static final ShovelItem WOODEN_SHOVEL = Items.register("wooden_shovel", new ShovelItem(new UV(0, 0), ItemMaterials.WOOD));

    private static <T extends Item> T register(String name, T block) {
        Registries.ITEMS.register(new Identifier(name), block);
        return block;
    }

    public static void nopInit() {

    }
}
