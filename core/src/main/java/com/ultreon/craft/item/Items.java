package com.ultreon.craft.item;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class Items {
    public static final Item AIR = register("air", new Item());
    public static final BlockItem GRASS_BLOCK = register("grass_block", new BlockItem(Blocks.GRASS_BLOCK));
    public static final BlockItem DIRT = register("dirt", new BlockItem(Blocks.DIRT));
    public static final BlockItem SAND = register("sand", new BlockItem(Blocks.SAND));
    public static final BlockItem STONE = register("stone", new BlockItem(Blocks.STONE));
    public static final BlockItem WATER = register("water", new BlockItem(Blocks.WATER));

    private static <T extends Item> T register(String name, T block) {
        Registries.ITEMS.register(new Identifier(name), block);
        return block;
    }

    public static void nopInit() {

    }
}
