package com.ultreon.craft.block;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.DelayedRegister;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.RegistrySupplier;
import com.ultreon.craft.render.UV;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.util.UtilityClass;

public final class Blocks extends UtilityClass {
    private static final DelayedRegister<Block> REGISTER = new DelayedRegister<>(UltreonCraft.NAMESPACE, Registries.BLOCK);
    public static final RegistrySupplier<Block> AIR = REGISTER.register("air", () -> new Block(0, null));
    public static final RegistrySupplier<Block> GRASS_BLOCK = REGISTER.register("grass_block", () -> new Block(1, new CubeModel(UV.blockUV(0, 0), UV.blockUV(2, 0), UV.blockUV(3, 0))));
    public static final RegistrySupplier<Block> DIRT_BLOCK = REGISTER.register("dirt", () -> new Block(2, new CubeModel(UV.blockUV(2, 0))));
    public static final RegistrySupplier<Block> STONE_BLOCK = REGISTER.register("stone", () -> new Block(3, new CubeModel(UV.blockUV(1, 0))));
    public static final RegistrySupplier<Block> WATER_BLOCK = REGISTER.register("water", () -> new Block(4, new CubeModel(UV.blockUV(14, 13))));

    public static void register() {
        REGISTER.register();
    }
}
