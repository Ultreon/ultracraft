package com.ultreon.craft.block;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.UV;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.util.UtilityClass;
import com.ultreon.libs.commons.v0.Identifier;

public final class Blocks extends UtilityClass {
    public static final Block AIR = register("air", new Block(0, null));

    public static final Block GRASS_BLOCK = register("grass_block", new Block(1, new CubeModel(UV.blockUV(0, 0), UV.blockUV(2, 0), UV.blockUV(3, 0))));

    public static final Block DIRT = register("dirt", new Block(2, new CubeModel(UV.blockUV(2, 0))));
    public static final Block SAND = register("sand", new Block(3, new CubeModel(UV.blockUV(4, 0))));
    public static final Block STONE = register("stone", new Block(4, new CubeModel(UV.blockUV(1, 0))));
    public static final Block WATER = register("water", new Block(5, true, new CubeModel(UV.blockUV(0, 1))));

    private static <T extends Block> T register(String name, T block) {
        Registries.BLOCK.register(new Identifier(name), block);
        return block;
    }

    public static void nopInit() {

    }
}
