package com.ultreon.craft.registry;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.font.Font;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import com.ultreon.libs.registries.v0.Registry;

public final class Registries {
    public static final Registry<Registry<?>> REGISTRIES = Registry.create(UltreonCraft.id("registry"));
    public static final Registry<Block> BLOCK = Registry.create(UltreonCraft.id("block"));
    public static final Registry<NoiseSettings> NOISE_SETTINGS = Registry.create(UltreonCraft.id("noise_settings"));
    public static final Registry<EntityType<?>> ENTITIES = Registry.create(UltreonCraft.id("entity_type"));
    public static final Registry<Font> FONTS = Registry.create(UltreonCraft.id("font"));
    public static final Registry<SoundEvent> SOUNDS = Registry.create(UltreonCraft.id("sound"));
    public static final Registry<ShaderProgram> SHADERS = Registry.create(UltreonCraft.id("shader"));

    public static void init() {
        REGISTRIES.register(REGISTRIES.id(), REGISTRIES);
        REGISTRIES.register(BLOCK.id(), BLOCK);
        REGISTRIES.register(NOISE_SETTINGS.id(), NOISE_SETTINGS);
        REGISTRIES.register(ENTITIES.id(), ENTITIES);
        REGISTRIES.register(FONTS.id(), FONTS);
        REGISTRIES.register(SOUNDS.id(), SOUNDS);
    }
}
