package com.ultreon.craft.registry;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.menu.MenuType;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.registries.v0.Registry;

public final class Registries {
    public static final Registry<Registry<?>> REGISTRIES = Registry.create(new Identifier("registry"));
    public static final Registry<Block> BLOCKS = Registry.create(new Identifier("block"));
    public static final Registry<Item> ITEMS = Registry.create(new Identifier("item"));
    public static final Registry<NoiseConfig> NOISE_SETTINGS = Registry.create(new Identifier("noise_settings"));
    public static final Registry<EntityType<?>> ENTITIES = Registry.create(new Identifier("entity_type"));
    public static final Registry<SoundEvent> SOUND_EVENTS = Registry.create(new Identifier("sound"));
    public static final Registry<MenuType<?>> MENU_TYPES = Registry.create(new Identifier("menu_type"));
    public static final Registry<Biome> BIOMES = Registry.create(new Identifier("biome"));

    public static void init() {
        Registries.REGISTRIES.register(Registries.REGISTRIES.id(), Registries.REGISTRIES);
        Registries.REGISTRIES.register(Registries.BLOCKS.id(), Registries.BLOCKS);
        Registries.REGISTRIES.register(Registries.NOISE_SETTINGS.id(), Registries.NOISE_SETTINGS);
        Registries.REGISTRIES.register(Registries.ENTITIES.id(), Registries.ENTITIES);
        Registries.REGISTRIES.register(Registries.SOUND_EVENTS.id(), Registries.SOUND_EVENTS);
    }
}
