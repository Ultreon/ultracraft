package com.ultreon.craft.registry;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.gen.noise.NoiseSettings;

public final class Registries {
    public static final Registry<Block> BLOCK = new Registry<>();
    public static final Registry<NoiseSettings> NOISE_SETTINGS = new Registry<>();
    public static final Registry<Registry<?>> REGISTRY = new Registry<>();

    public static void init() {

    }
}
