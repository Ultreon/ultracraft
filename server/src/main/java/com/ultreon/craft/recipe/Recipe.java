package com.ultreon.craft.recipe;

import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.List;

public interface Recipe {
    ItemStack craft(Inventory inventory);

    boolean canCraft(Inventory inventory);

    RecipeType getType();

    ItemStack result();

    default Identifier getId() {
        return RecipeManager.get().getKey(this.getType(), this);
    }

    List<ItemStack> ingredients();
}
