package com.ultreon.craft.registry;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import com.ultreon.libs.registries.v0.Registry;

public final class Registries {
    public static final Registry<Block> BLOCK = Registry.create(UltreonCraft.id("block"));
    public static final Registry<NoiseSettings> NOISE_SETTINGS = Registry.create(UltreonCraft.id("noise_settings"));
    public static final Registry<Registry<?>> REGISTRY = Registry.create(UltreonCraft.id("registry"));

    public static void init() {

    }
}
