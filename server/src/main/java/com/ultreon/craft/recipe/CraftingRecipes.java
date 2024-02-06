package com.ultreon.craft.recipe;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.util.ElementID;

import java.util.List;

public class CraftingRecipes {
    public static void init() {
        RecipeManager rm = RecipeManager.get();
        rm.register(new ElementID("log_to_planks"), new CraftingRecipe(
                List.of(new ItemStack(Items.LOG)),
                new ItemStack(Items.PLANK, 3)));

        rm.register(new ElementID("planks_to_stick"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.STICK, 4)));

        rm.register(new ElementID("planks_to_block"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 4)),
                new ItemStack(Items.PLANKS)));

        rm.register(new ElementID("wooden_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 3)),
                new ItemStack(Items.WOODEN_PICKAXE)));

        rm.register(new ElementID("wooden_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.WOODEN_SHOVEL)));

        rm.register(new ElementID("wooden_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 2)),
                new ItemStack(Items.WOODEN_AXE)));

        rm.register(new ElementID("crafting_bench"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8)),
                new ItemStack(Items.CRAFTING_BENCH)));

        rm.register(new ElementID("rock_to_cobblestone"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 4)),
                new ItemStack(Items.COBBLESTONE)));

        rm.register(new ElementID("stone_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 3), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_PICKAXE)));

        rm.register(new ElementID("crate"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8), new ItemStack(Items.STICK, 2)),
                new ItemStack(Items.CRATE)));

        rm.register(new ElementID("stone_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 1), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_SHOVEL)));

        rm.register(new ElementID("stone_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 2), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_AXE)));
    }
}
