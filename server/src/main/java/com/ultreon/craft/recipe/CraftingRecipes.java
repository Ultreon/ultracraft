package com.ultreon.craft.recipe;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.util.Identifier;

import java.util.List;

public class CraftingRecipes {
    public static void init() {
        RecipeManager rm = RecipeManager.get();
        rm.register(new Identifier("log_to_planks"), new CraftingRecipe(
                List.of(new ItemStack(Items.LOG)),
                new ItemStack(Items.PLANK, 3)));

        rm.register(new Identifier("planks_to_stick"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.STICK, 4)));

        rm.register(new Identifier("planks_to_block"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 4)),
                new ItemStack(Items.PLANKS)));

        rm.register(new Identifier("wooden_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 3)),
                new ItemStack(Items.WOODEN_PICKAXE)));

        rm.register(new Identifier("wooden_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.WOODEN_SHOVEL)));

        rm.register(new Identifier("wooden_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 2)),
                new ItemStack(Items.WOODEN_AXE)));

        rm.register(new Identifier("crafting_bench"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8)),
                new ItemStack(Items.CRAFTING_BENCH)));

        rm.register(new Identifier("rock_to_cobblestone"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 4)),
                new ItemStack(Items.COBBLESTONE)));

        rm.register(new Identifier("stone_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 3), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_PICKAXE)));

        rm.register(new Identifier("crate"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8), new ItemStack(Items.STICK, 2)),
                new ItemStack(Items.CRATE)));

        rm.register(new Identifier("stone_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 1), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_SHOVEL)));

        rm.register(new Identifier("stone_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 2), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_AXE)));
    }
}
