package com.ultreon.craft.block;

import com.ultreon.craft.block.Block.Properties;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.item.tool.ToolType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.loot.RandomLoot;
import org.apache.commons.lang3.IntegerRange;

public final class Blocks {
    public static final Block AIR = Blocks.register("air", new Block(new Properties().replaceable().noCollision().noRendering().transparent()));
    public static final Block CAVE_AIR = Blocks.register("cave_air", new Block(new Properties().replaceable().noCollision().noRendering().transparent()));
    public static final Block BARRIER = Blocks.register("barrier", new Block(new Properties().transparent().noRendering()));
    public static final Block ERROR = Blocks.register("error", new Block(new Properties()));
    public static final Block GRASS_BLOCK = Blocks.register("grass_block", new Block(new Properties().hardness(3F).effectiveTool(ToolType.SHOVEL).dropsItems(Items.DIRT)));
    public static final Block DIRT = Blocks.register("dirt", new Block(new Properties().hardness(3F).effectiveTool(ToolType.SHOVEL).dropsItems(Items.DIRT)));
    public static final Block VOIDGUARD = Blocks.register("voidguard", new Block(new Properties().unbreakable().effectiveTool(ToolType.PICKAXE).dropsItems(new Item[0])));
    public static final Block SAND = Blocks.register("sand", new Block(new Properties().hardness(2.5F).effectiveTool(ToolType.SHOVEL).dropsItems(Items.SAND)));
    public static final Block GRAVEL = Blocks.register("gravel", new Block(new Properties().hardness(2.7F).effectiveTool(ToolType.SHOVEL).dropsItems(Items.GRAVEL)));
    public static final Block STONE = Blocks.register("stone", new Block(new Properties().hardness(12.0F).effectiveTool(ToolType.PICKAXE).requiresTool().dropsItems(new RandomLoot(new RandomLoot.CountLootEntry(IntegerRange.of(2, 4), Items.ROCK)))));
    public static final Block COBBLESTONE = Blocks.register("cobblestone", new Block(new Properties().hardness(11.0F).effectiveTool(ToolType.PICKAXE).requiresTool().dropsItems(Items.COBBLESTONE)));
    public static final Block SANDSTONE = Blocks.register("sandstone", new Block(new Properties().hardness(8.0F).effectiveTool(ToolType.PICKAXE).requiresTool().dropsItems(Items.SANDSTONE)));
    public static final Block WATER = Blocks.register("water", new Block(new Properties().noCollision().fluid().hardness(120.0F)));
    public static final Block LOG = Blocks.register("log", new Block(new Properties().hardness(2.0F).effectiveTool(ToolType.AXE).dropsItems(Items.LOG)));
    public static final Block PLANKS = Blocks.register("planks", new Block(new Properties().hardness(2.0F).effectiveTool(ToolType.AXE).dropsItems(Items.PLANKS)));
    public static final Block LEAVES = Blocks.register("leaves", new Block(new Properties().hardness(0.2F)));
    public static final Block CRAFTING_BENCH = Blocks.register("crafting_bench", new Block(new Properties().hardness(3.0F).effectiveTool(ToolType.AXE).dropsItems(Items.CRAFTING_BENCH)));
    public static final Block TALL_GRASS = Blocks.register("tall_grass", new Block(new Properties().instaBreak().noOcclude().replaceable().transparent().noCollision().usesCustomRender().effectiveTool(ToolType.AXE).dropsItems(new RandomLoot(new RandomLoot.ChanceLootEntry(0.4f, Items.GRASS_FIBRE)))));

    private static <T extends Block> T register(String name, T block) {
        Registries.BLOCK.register(new ElementID(name), block);
        return block;
    }

    public static void nopInit() {
        // Load class
    }
}
