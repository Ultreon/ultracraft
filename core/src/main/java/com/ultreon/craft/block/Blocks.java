package com.ultreon.craft.block;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.UV;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.util.UtilityClass;
import com.ultreon.libs.commons.v0.Identifier;

public final class Blocks extends UtilityClass {
    public static final Block AIR = nopInit("air", new Block(0, null));

    public static final Block GRASS_BLOCK = nopInit("grass_block", new Block(1, new CubeModel(UV.blockUV(0, 0), UV.blockUV(2, 0), UV.blockUV(3, 0))));

    public static final Block DIRT = nopInit("dirt", new Block(2, new CubeModel(UV.blockUV(2, 0))));
    public static final Block SAND = nopInit("stone", new Block(3, new CubeModel(UV.blockUV(2, 1))));
    public static final Block STONE = nopInit("stone", new Block(3, new CubeModel(UV.blockUV(1, 0))));
    public static final Block WATER = nopInit("water", new Block(4, new CubeModel(UV.blockUV(14, 13))));

    private static <T extends Block> T nopInit(String name, T block) {
        Registries.BLOCK.register(new Identifier(name), block);
        return block;
    }

    public static void nopInit() {

    }
}
