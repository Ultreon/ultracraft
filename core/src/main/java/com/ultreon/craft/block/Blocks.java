package com.ultreon.craft.block;

import com.ultreon.craft.block.Block.Properties;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.UtilityClass;
import com.ultreon.libs.commons.v0.Identifier;

public final class Blocks extends UtilityClass {
    public static final Block AIR = register("air", new Block());

    public static final Block GRASS_BLOCK = register("grass_block", new Block());

    public static final Block DIRT = register("dirt", new Block());
    public static final Block SAND = register("sand", new Block());
    public static final Block STONE = register("stone", new Block());
    public static final Block WATER = register("water", new Block(new Properties().transparent().noCollision()));

    private static <T extends Block> T register(String name, T block) {
        Registries.BLOCK.register(new Identifier(name), block);
        return block;
    }

    public static void nopInit() {

    }
}
