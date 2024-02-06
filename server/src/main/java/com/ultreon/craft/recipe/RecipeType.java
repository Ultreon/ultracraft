package com.ultreon.craft.recipe;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;

public class RecipeType {
    public static final RecipeType CRAFTING = RecipeType.register("crafting", new RecipeType());

    private static RecipeType register(String crafting, RecipeType recipeType) {
        Registries.RECIPE_TYPE.register(new ElementID(crafting), recipeType);
        return recipeType;
    }

    public ElementID getKey() {
        return Registries.RECIPE_TYPE.getId(this);
    }

    public int getId() {
        return Registries.RECIPE_TYPE.getRawId(this);
    }
}
