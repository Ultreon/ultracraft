package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.UtilityClass;
import com.ultreon.libs.registries.v0.DelayedRegister;
import com.ultreon.libs.registries.v0.RegistrySupplier;

public final class NoiseSettingsInit extends UtilityClass {
    private static final DelayedRegister<NoiseSettings> REGISTER = DelayedRegister.create(UltreonCraft.NAMESPACE, Registries.NOISE_SETTINGS);

    public static final RegistrySupplier<NoiseSettings> DEFAULT = REGISTER.register("default", () ->
              new NoiseSettings(0.01f, 5, new Vector2(-100, 3400), 98473, 0.5f, 0.1f, 0.44f));

    public static final RegistrySupplier<NoiseSettings> TREE = REGISTER.register("tree", () ->
            new NoiseSettings(0.01f, 1, new Vector2(300, 5000), 4973995, 0.01f, 1.2f, 4f));

    public static final RegistrySupplier<NoiseSettings> STONE_PATCH = REGISTER.register("stone_patch", () ->
            new NoiseSettings(0.01f, 5, new Vector2(-48000, 85000), 1959373, 0.5f, 0.25f, 0.635f));

    public static final RegistrySupplier<NoiseSettings> DOMAIN_X = REGISTER.register("domain_x", () ->
            new NoiseSettings(0.02f, 3, new Vector2(600, 350), 998334, 0.5f, 1.2f, 5f));

    public static final RegistrySupplier<NoiseSettings> DOMAIN_Y = REGISTER.register("domain_y", () ->
            new NoiseSettings(0.02f, 3, new Vector2(900, 1500), 9854449, 0.5f, 1.2f, 5f));

    public static void register() {
        REGISTER.register();
    }
}
