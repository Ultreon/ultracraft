package com.ultreon.craft.registry;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.Attribute;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.menu.MenuType;
import com.ultreon.craft.weather.Weather;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.registries.v0.Registry;

public final class Registries {
    public static final Registry<Registry<?>> REGISTRY = Registry.create(new Identifier("registry"));
    public static final Registry<Block> BLOCK = Registries.create(new Identifier("block"));
    public static final Registry<Item> ITEM = Registries.create(new Identifier("item"));
    public static final Registry<NoiseConfig> NOISE_SETTINGS = Registries.create(new Identifier("noise_settings"));
    public static final Registry<EntityType<?>> ENTITY_TYPE = Registries.create(new Identifier("entity_type"));
    public static final Registry<SoundEvent> SOUND_EVENT = Registries.create(new Identifier("sound"));
    public static final Registry<MenuType<?>> MENU_TYPE = Registries.create(new Identifier("menu_type"));
    public static final Registry<Biome> BIOME = Registries.create(new Identifier("biome"));
    public static final Registry<Weather> WEATHER = Registries.create(new Identifier("weather"));
    public static final Registry<Attribute> ATTRIBUTE = Registries.create(new Identifier("attribute"));

    public static void nopInit() {

    }

    public static <T> Registry<T> create(Identifier id, T... typeGetter) {
        Registry<T> registry = Registry.create(id, typeGetter);
        Registries.REGISTRY.register(id, registry);
        return registry;
    }
}
