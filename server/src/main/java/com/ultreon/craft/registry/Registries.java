package com.ultreon.craft.registry;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.Attribute;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.menu.MenuType;
import com.ultreon.craft.recipe.RecipeType;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.weather.Weather;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.gen.noise.NoiseConfig;

public final class Registries {
    public static final Registry<Registry<?>> REGISTRY = Registry.<Registry<?>>builder(new ElementID("registry")).build();
    public static final Registry<Block> BLOCK = Registries.create(new ElementID("block"));
    public static final Registry<Item> ITEM = Registries.create(new ElementID("item"));
    public static final Registry<NoiseConfig> NOISE_SETTINGS = Registries.create(new ElementID("noise_settings"));
    public static final Registry<EntityType<?>> ENTITY_TYPE = Registries.create(new ElementID("entity_type"));
    public static final Registry<SoundEvent> SOUND_EVENT = Registries.create(new ElementID("sound"));
    public static final Registry<MenuType<?>> MENU_TYPE = Registries.create(new ElementID("menu_type"));
    public static final Registry<Biome> BIOME = Registries.create(new ElementID("biome"));
    public static final Registry<Weather> WEATHER = Registries.create(new ElementID("weather"));
    public static final Registry<Attribute> ATTRIBUTE = Registries.create(new ElementID("attribute"));
    public static final Registry<DamageSource> DAMAGE_SOURCE = Registries.create(new ElementID("damage_source"));
    public static final Registry<RecipeType> RECIPE_TYPE = Registries.create(new ElementID("recipe_type"));

    public static void nopInit() {
        // Load class
    }

    public static <T> Registry<T> create(ElementID id, T... typeGetter) {
        return Registry.builder(id, typeGetter).build();
    }
}
