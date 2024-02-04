package com.ultreon.craft.recipe;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.menu.ItemSlot;

import java.util.List;
import java.util.stream.Collectors;

public record CraftingRecipe(List<ItemStack> ingredients, ItemStack result) implements Recipe {

    @Override
    public ItemStack craft(Inventory inventory) {
        var result = this.result.copy();
        var ingredients = this.ingredients.stream().map(ItemStack::copy).collect(Collectors.toList());

        for (ItemSlot slot : inventory.slots) {
            if (slot.isEmpty()) {
                continue;
            }

            CraftingRecipe.collectItems(slot, ingredients, false);

            if (ingredients.isEmpty()) {
                return result;
            }
        }

        return null;
    }

    @Override
    public boolean canCraft(Inventory inventory) {
        var ingredients = this.ingredients.stream().map(ItemStack::copy).collect(Collectors.toList());

        for (ItemSlot slot : inventory.slots) {
            if (slot.isEmpty()) {
                continue;
            }

            CraftingRecipe.collectItems(slot, ingredients, true);

            if (ingredients.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static void collectItems(ItemSlot slot, List<ItemStack> copy, boolean simulate) {
        int i = 0;
        while (i < copy.size()) {
            if (copy.get(i).isSimilar(slot.getItem())) {
                int count = slot.getItem().getCount();
                int actualAmount = copy.get(i).shrink(count);
                if (!simulate) {
                    slot.shrink(count - actualAmount);
                }

                if (copy.get(i).isEmpty()) {
                    copy.remove(i);
                    return;
                }
            }
            i++;
        }
    }

    @Override
    public RecipeType getType() {
        return RecipeType.CRAFTING;
    }
}
