package com.ultreon.craft.registry;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.entity.Attribute;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.menu.MenuType;
import com.ultreon.craft.recipe.RecipeType;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.weather.Weather;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.gen.noise.NoiseConfig;

public class RegistryKeys {
    public static final RegistryKey<Registry<Block>> BLOCK = RegistryKey.registry(new Identifier("block"));
    public static final RegistryKey<Registry<Item>> ITEM = RegistryKey.registry(new Identifier("item"));
    public static final RegistryKey<Registry<NoiseConfig>> NOISE_SETTINGS = RegistryKey.registry(new Identifier("noise_settings"));
    public static final RegistryKey<Registry<EntityType<?>>> ENTITY_TYPE = RegistryKey.registry(new Identifier("entity_type"));
    public static final RegistryKey<Registry<SoundEvent>> SOUND_EVENT = RegistryKey.registry(new Identifier("sound"));
    public static final RegistryKey<Registry<MenuType<?>>> MENU_TYPE = RegistryKey.registry(new Identifier("menu_type"));
    public static final RegistryKey<Registry<Biome>> BIOME = RegistryKey.registry(new Identifier("biome"));
    public static final RegistryKey<Registry<Weather>> WEATHER = RegistryKey.registry(new Identifier("weather"));
    public static final RegistryKey<Registry<Attribute>> ATTRIBUTE = RegistryKey.registry(new Identifier("attribute"));
    public static final RegistryKey<Registry<DamageSource>> DAMAGE_SOURCE = RegistryKey.registry(new Identifier("damage_source"));
    public static final RegistryKey<Registry<RecipeType<?>>> RECIPE_TYPE = RegistryKey.registry(new Identifier("recipe_type"));
    public static final RegistryKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE = RegistryKey.registry(new Identifier("block_entity_type"));
}
