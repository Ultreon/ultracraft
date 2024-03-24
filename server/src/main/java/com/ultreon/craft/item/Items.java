package com.ultreon.craft.item;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.item.Item.Properties;
import com.ultreon.craft.item.material.ItemMaterials;
import com.ultreon.craft.item.tool.AxeItem;
import com.ultreon.craft.item.tool.PickaxeItem;
import com.ultreon.craft.item.tool.ShovelItem;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;

public class Items {
    public static final Item AIR = Items.register("air", new Item(new Properties()));
    public static final BlockItem GRASS_BLOCK = Items.register("grass_block", new BlockItem(new Properties(), () -> Blocks.GRASS_BLOCK));
    public static final BlockItem DIRT = Items.register("dirt", new BlockItem(new Properties(), () -> Blocks.DIRT));
    public static final BlockItem SAND = Items.register("sand", new BlockItem(new Properties(), () -> Blocks.SAND));
    public static final BlockItem GRAVEL = Items.register("gravel", new BlockItem(new Properties(), () -> Blocks.GRAVEL));
    public static final BlockItem STONE = Items.register("stone", new BlockItem(new Properties(), () -> Blocks.STONE));
    public static final BlockItem COBBLESTONE = Items.register("cobblestone", new BlockItem(new Properties(), () -> Blocks.COBBLESTONE));
    public static final BlockItem SANDSTONE = Items.register("sandstone", new BlockItem(new Properties(), () -> Blocks.SANDSTONE));
    public static final BlockItem WATER = Items.register("water", new BlockItem(new Properties(), () -> Blocks.WATER));
    public static final BlockItem LOG = Items.register("log", new BlockItem(new Properties(), () -> Blocks.LOG));
    public static final Item PLANK = Items.register("plank", new Item(new Properties()));
    public static final BlockItem PLANKS = Items.register("planks", new BlockItem(new Properties(), () -> Blocks.PLANKS));
    public static final BlockItem CRATE = Items.register("crate", new BlockItem(new Properties(), () -> Blocks.CRATE));
    public static final PickaxeItem WOODEN_PICKAXE = Items.register("wooden_pickaxe", new PickaxeItem(new Properties().stackSize(1), ItemMaterials.WOOD));
    public static final ShovelItem WOODEN_SHOVEL = Items.register("wooden_shovel", new ShovelItem(new Properties().stackSize(1), ItemMaterials.WOOD));
    public static final AxeItem WOODEN_AXE = Items.register("wooden_axe", new AxeItem(new Properties().stackSize(1), ItemMaterials.WOOD));
    public static final PickaxeItem STONE_PICKAXE = Items.register("stone_pickaxe", new PickaxeItem(new Properties().stackSize(1), ItemMaterials.STONE));
    public static final ShovelItem STONE_SHOVEL = Items.register("stone_shovel", new ShovelItem(new Properties().stackSize(1), ItemMaterials.STONE));
    public static final AxeItem STONE_AXE = Items.register("stone_axe", new AxeItem(new Properties().stackSize(1), ItemMaterials.STONE));
    public static final BlockItem CRAFTING_BENCH = Items.register("crafting_bench", new BlockItem(new Properties(), () -> Blocks.CRAFTING_BENCH));
    public static final Item STICK = Items.register("stick", new Item(new Properties()));
    public static final Item ROCK = Items.register("rock", new Item(new Properties()));
    public static final Item GRASS_FIBRE = Items.register("grass_fibre", new Item(new Properties()));
    public static final BlockItem CACTUS = Items.register("cactus", new BlockItem(new Properties(), () -> Blocks.CACTUS));

    private static <T extends Item> T register(String name, T block) {
        Registries.ITEM.register(new Identifier(name), block);
        return block;
    }

    public static void nopInit() {

    }
}
