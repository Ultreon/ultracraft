package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.Biome;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec2f;

public final class NoiseConfigs {
    public static final NoiseConfig PLAINS = NoiseConfigs.register("plains",
            new NoiseConfig(0.001f, 6, new Vec2f(330462, 631774), 196977, .35f, .001f, 1f, 30, 120));

    public static final NoiseConfig OCEAN = NoiseConfigs.register("ocean",
            new NoiseConfig(0.001f, 6, new Vec2f(-104449, -342629), -45258, .35f, .001f, 1f, 30, 60));

    public static final NoiseConfig DEFAULT = NoiseConfigs.register("default",
            new NoiseConfig(0.001f, 6, new Vec2f(200, 740), 128436, .35f, .001f, 1f, 40, 60));

    public static final NoiseConfig TREE = NoiseConfigs.register("tree",
            new NoiseConfig(0.01f, 1, new Vec2f(300, 5000), 497395, 0.01f, 1.2f, 4f, 1, 0));

    public static final NoiseConfig STONE_PATCH = NoiseConfigs.register("stone_patch",
            new NoiseConfig(0.01f, 5, new Vec2f(680875, 914213), 425023, 0.5f, 0.25f, 0.635f, 1, 0.5f));

    public static final NoiseConfig SAND_PATCH = NoiseConfigs.register("sand_patch",
            new NoiseConfig(0.01f, 5, new Vec2f(680875, 914213), 425023, 0.5f, 0.25f, 0.635f, 1, 0.5f));

    public static final NoiseConfig BIOME_X = NoiseConfigs.register("biome_x",
            new NoiseConfig(0.001f, 4, new Vec2f(69400, 35350), 998334, 0.6f, 1.2f, 5f, 30, 45));

    public static final NoiseConfig BIOME_Y = NoiseConfigs.register("biome_y",
            new NoiseConfig(0.001f, 4, new Vec2f(35900, 15900), 985449, 0.6f, 1.2f, 5f, 30, 45));

    public static final NoiseConfig LAYER_X = NoiseConfigs.register("layer_x",
            new NoiseConfig(0.003f, 4, new Vec2f(69400, 35350), 998334, 0.6f, 1.2f, 5f, 40, 65));

    public static final NoiseConfig LAYER_Y = NoiseConfigs.register("layer_y",
            new NoiseConfig(0.003f, 4, new Vec2f(35900, 15900), 985449, 0.6f, 1.2f, 5f, 40, 65));
    public static final NoiseConfig BIOME_MAP = NoiseConfigs.register("biome_map",
            new NoiseConfig(0.0004f, 2, new Vec2f(903852, 493382), 137339, 0.6f, 1.2f, 5f, 10, 0));
    public static final NoiseConfig OVERWORLD_NOISE = NoiseConfigs.register("overworld_dimension", new NoiseConfig(0.1f, 8, new Vec2f(491333, 998360), 388691, 0.6f, 1.5f, 5f, 80, 64));
    public static final NoiseConfig CAVE_NOISE = NoiseConfigs.register("cave", new NoiseConfig(1f, 1, new Vec2f(491333, 998360), 388691, 0.6f, 1.5f, 5f, 0, 0));

    private static <T extends NoiseConfig> T register(String name, T settings) {
        Registries.NOISE_SETTINGS.register(new Identifier(UltracraftServer.NAMESPACE, name), settings);
        return settings;
    }

    public static void nopInit() {

    }
}
