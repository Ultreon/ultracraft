package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.UtilityClass;
import com.ultreon.libs.commons.v0.Identifier;

public final class NoiseSettingsInit extends UtilityClass {
    public static final NoiseSettings BIOME_PLAINS = register("biome/plains",
            new NoiseSettings(0.01F, 6, new Vector2(6389, -3359), 0x37fe8ca9e8f9c9f7L, 0.350F, 0.001F, 0.16F));
    public static final NoiseSettings BIOME_DESERT = register("biome/desert",
            new NoiseSettings(0.01F, 5, new Vector2(6389, -3359), 0x37fe8ca9e8f9c9f7L, 0.400F, 0.200F, 0.44F));
    public static final NoiseSettings BIOME_SEA = register("biome/sea",
            new NoiseSettings(0.01F, 5, new Vector2(6389, -3359), 0x37fe8ca9e8f9c9f7L, 0.002F, 0.002F, 0.44F));
    public static final NoiseSettings BIOME_DEEP_SEA = register("biome/deep_sea",
            new NoiseSettings(0.01F, 5, new Vector2(6389, -3359), 0x37fe8ca9e8f9c9f7L, 0.300F, 0.035F, 0.50F));
    public static final NoiseSettings TERRAIN = register("terrain",
            new NoiseSettings(0.01F, 6, new Vector2(-2313, 8543), 0x83d7ca8bf830e83fL, 0.350F, 0.001F, 0.16F));
    public static final NoiseSettings TREE = register("tree",
            new NoiseSettings(0.01F, 1, new Vector2(3263, 5058), 0x48f493e48f389bc9L, 0.010F, 1.200F, 4.00F));
    public static final NoiseSettings STONE_PATCH = register("stone_patch",
            new NoiseSettings(0.1F, 5, new Vector2(-4834, 8548), 0xca98e39fd8e0888aL, 0.500F, 1.200F, 4.00F));
    public static final NoiseSettings DOMAIN_X = register("domain_x",
            new NoiseSettings(0.02F, 3, new Vector2(-6564, -3658), 0xe834f89c0a749ff3L, 0.500F, 1.200F, 5.00F));
    public static final NoiseSettings DOMAIN_Y = register("domain_y",
            new NoiseSettings(0.02F, 3, new Vector2(9926, 1584), 0x28f8ec99a7e9f8ecL, 0.500F, 1.200F, 5.00F));

    private static <T extends NoiseSettings> T register(String name, T settings) {
        Registries.NOISE_SETTINGS.register(new Identifier(UltreonCraft.NAMESPACE, name), settings);
        return settings;
    }

    public static void nopInit() {

    }
}
