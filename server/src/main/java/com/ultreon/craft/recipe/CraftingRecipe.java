package com.ultreon.craft.recipe;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.util.Identifier;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;

import java.util.ArrayList;
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
            if (copy.get(i).sameItemSameData(slot.getItem())) {
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
    public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    public static CraftingRecipe deserialize(Identifier identifier, Json5Object object) {
        List<ItemStack> ingredients = new ArrayList<>();

        for (Json5Element json5Element : object.getAsJson5Array("ingredients")) {
            Json5Object asJson5Object = json5Element.getAsJson5Object();
            ItemStack itemStack = ItemStack.deserialize(asJson5Object);

            ingredients.add(itemStack);
        }

        ItemStack result = ItemStack.deserialize(object.getAsJson5Object("result"));

        return new CraftingRecipe(ingredients, result);
    }
}
