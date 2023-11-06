package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.CommonConstants;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec2f;

public final class NoiseSettingsInit {
    private NoiseSettingsInit() {

    }

    public static final NoiseSettings DEFAULT = NoiseSettingsInit.register("default",
            new NoiseSettings(0.1f, 6, new Vec2f(200, 740), 128436, .35f, .001f, 1f, 40, 70));

    public static final NoiseSettings TREE = NoiseSettingsInit.register("tree",
            new NoiseSettings(0.01f, 1, new Vec2f(300, 5000), 497395, 0.01f, 1.2f, 4f, 1, 0));

    public static final NoiseSettings STONE_PATCH = NoiseSettingsInit.register("stone_patch",
            new NoiseSettings(0.01f, 5, new Vec2f(-48000, 85000), 159373, 0.5f, 0.25f, 0.635f, 1, 0.5f));

    public static final NoiseSettings DOMAIN_X = NoiseSettingsInit.register("domain_x",
            new NoiseSettings(0.01f, 4, new Vec2f(69400, 35350), 998334, 0.6f, 1.2f, 5f, 30, 60));

    public static final NoiseSettings DOMAIN_Y = NoiseSettingsInit.register("domain_y",
            new NoiseSettings(0.01f, 4, new Vec2f(35900, 15900), 985449, 0.6f, 1.2f, 5f, 30, 60));

    private static <T extends NoiseSettings> T register(String name, T settings) {
        Registries.NOISE_SETTINGS.register(new Identifier(CommonConstants.NAMESPACE, name), settings);
        return settings;
    }

    public static void nopInit() {

    }
}
