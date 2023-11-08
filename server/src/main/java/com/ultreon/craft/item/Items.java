package com.ultreon.craft.item;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.item.Item.Properties;
import com.ultreon.craft.item.material.ItemMaterials;
import com.ultreon.craft.item.tool.PickaxeItem;
import com.ultreon.craft.item.tool.ShovelItem;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class Items {
    public static final Item AIR = Items.register("air", new Item(new Properties()));
    public static final BlockItem GRASS_BLOCK = Items.register("grass_block", new BlockItem(new Properties(), () -> Blocks.GRASS_BLOCK));
    public static final BlockItem DIRT = Items.register("dirt", new BlockItem(new Properties(), () -> Blocks.DIRT));
    public static final BlockItem SAND = Items.register("sand", new BlockItem(new Properties(), () -> Blocks.SAND));
    public static final BlockItem STONE = Items.register("stone", new BlockItem(new Properties(), () -> Blocks.STONE));
    public static final BlockItem COBBLESTONE = Items.register("cobblestone", new BlockItem(new Properties(), () -> Blocks.COBBLESTONE));
    public static final BlockItem SANDSTONE = Items.register("sandstone", new BlockItem(new Properties(), () -> Blocks.SANDSTONE));
    public static final BlockItem WATER = Items.register("water", new BlockItem(new Properties(), () -> Blocks.WATER));
    public static final PickaxeItem WOODEN_PICKAXE = Items.register("wooden_pickaxe", new PickaxeItem(new Properties().stackSize(1), ItemMaterials.WOOD));
    public static final ShovelItem WOODEN_SHOVEL = Items.register("wooden_shovel", new ShovelItem(new Properties().stackSize(1), ItemMaterials.WOOD));

    private static <T extends Item> T register(String name, T block) {
        Registries.ITEMS.register(new Identifier(name), block);
        return block;
    }

    public static void nopInit() {

    }
}
