package com.ultreon.craft.recipe;

import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class RecipeType {
    public static final RecipeType CRAFTING = RecipeType.register("crafting", new RecipeType());

    private static RecipeType register(String crafting, RecipeType recipeType) {
        Registries.RECIPE_TYPE.register(new Identifier(crafting), recipeType);
        return recipeType;
    }

    public Identifier getKey() {
        return Registries.RECIPE_TYPE.getKey(this);
    }

    public int getId() {
        return Registries.RECIPE_TYPE.getId(this);
    }
}
