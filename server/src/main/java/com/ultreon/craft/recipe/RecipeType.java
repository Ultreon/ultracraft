package com.ultreon.craft.recipe;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;
import de.marhali.json5.Json5Object;

/**
 * Represents a type of recipe.
 *
 * @param <T> The type of recipe
 */
public record RecipeType<T extends Recipe>(RecipeType.RecipeDeserializer<T> deserializer) {
    public static final RecipeType<CraftingRecipe> CRAFTING = RecipeType.register("crafting", new RecipeType<>(CraftingRecipe::deserialize));

    /**
     * Registers a recipe type.
     *
     * @param name       The name of the recipe type
     * @param recipeType The recipe type to register
     * @param <T>        The type of recipe
     * @return The registered recipe type
     */
    private static <T extends Recipe> RecipeType<T> register(String name, RecipeType<T> recipeType) {
        Registries.RECIPE_TYPE.register(new Identifier(name), recipeType);
        return recipeType;
    }

    /**
     * Gets the identifier key of the recipe type.
     *
     * @return The identifier key
     */
    public Identifier getKey() {
        return Registries.RECIPE_TYPE.getId(this);
    }

    /**
     * Gets the ID of the recipe type.
     *
     * @return The ID of the recipe type
     */
    public int getId() {
        return Registries.RECIPE_TYPE.getRawId(this);
    }

    /**
     * Deserializes a recipe of the given type.
     *
     * @param id   The identifier of the recipe
     * @param root The JSON object containing the recipe data
     * @return The deserialized recipe
     */
    public T deserialize(Identifier id, Json5Object root) {
        return this.deserializer.deserialize(id, root);
    }

    /**
     * Functional interface for deserializing a recipe.
     *
     * @param <T> The type of recipe to deserialize
     */
    @FunctionalInterface
    public interface RecipeDeserializer<T extends Recipe> {
        /**
         * Deserializes a recipe of the given type.
         *
         * @param id   The identifier of the recipe
         * @param root The JSON object containing the recipe data
         * @return The deserialized recipe
         */
        T deserialize(Identifier id, Json5Object root);
    }
}
