package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.DelayedRegister;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.RegistrySupplier;
import com.ultreon.craft.util.UtilityClass;
import com.ultreon.craft.util.Vec2i;

public final class NoiseSettingsInit extends UtilityClass {
    private static final DelayedRegister<NoiseSettings> REGISTER = new DelayedRegister<>(UltreonCraft.NAMESPACE, Registries.NOISE_SETTINGS);
    
    public static final RegistrySupplier<NoiseSettings> DEFAULT = REGISTER.register("default", () -> 
            new NoiseSettings(0.01f, 5, new Vec2i(-100, 3400), new Vec2i(0, 0), 0.5f, 0.1f, 0.44f));
    
    public static final RegistrySupplier<NoiseSettings> TREE = REGISTER.register("tree", () -> 
            new NoiseSettings(0.01f, 1, new Vec2i(300, 5000), new Vec2i(0, 0), 0.01f, 1.2f, 4f));
    
    public static final RegistrySupplier<NoiseSettings> STONE_PATCH = REGISTER.register("stone_patch", () ->
            new NoiseSettings(0.01f, 5, new Vec2i(-48303, 85746), new Vec2i(0, 0), 0.5f, 0.25f, 0.635f));
    
    public static final RegistrySupplier<NoiseSettings> DOMAIN_X = REGISTER.register("domain_x", () ->
            new NoiseSettings(0.02f, 3, new Vec2i(600, 350), new Vec2i(0, 0), 0.5f, 1.2f, 5f));
    
    public static final RegistrySupplier<NoiseSettings> DOMAIN_Y = REGISTER.register("domain_y", () ->
            new NoiseSettings(0.02f, 3, new Vec2i(900, 1500), new Vec2i(0, 0), 0.5f, 1.2f, 5f));

    public static void register() {
        REGISTER.register();
    }
}
