package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.UtilityClass;
import com.ultreon.libs.commons.v0.Identifier;

public final class NoiseSettingsInit extends UtilityClass {
    public static final NoiseSettings DEFAULT = register("default",
            new NoiseSettings(0.02f, 6, new Vector2(200, 740), 128436, .5f, 1.2f, 5f));

    public static final NoiseSettings TREE = register("tree",
            new NoiseSettings(0.01f, 1, new Vector2(300, 5000), 497395, 0.01f, 1.2f, 4f));

    public static final NoiseSettings STONE_PATCH = register("stone_patch",
            new NoiseSettings(0.01f, 5, new Vector2(-48000, 85000), 159373, 0.5f, 0.25f, 0.635f));

    public static final NoiseSettings DOMAIN_X = register("domain_x",
            new NoiseSettings(0.01f, 3, new Vector2(600, 350), 998334, 0.5f, 1.2f, 5f));

    public static final NoiseSettings DOMAIN_Y = register("domain_y",
            new NoiseSettings(0.01f, 3, new Vector2(900, 1500), 985449, 0.5f, 1.2f, 5f));

    private static <T extends NoiseSettings> T register(String name, T settings) {
        Registries.NOISE_SETTINGS.register(new Identifier(UltreonCraft.NAMESPACE, name), settings);
        return settings;
    }

    public static void nopInit() {

    }
}
