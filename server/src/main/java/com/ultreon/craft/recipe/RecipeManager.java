package com.ultreon.craft.recipe;

import com.badlogic.gdx.utils.IdentityMap;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.util.PagedList;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class RecipeManager {
    private static final RecipeManager INSTANCE = new RecipeManager();
    private final IdentityMap<RecipeType, RecipeRegistry<Recipe>> registryMap = new IdentityMap<>();

    private RecipeManager() { }

    public <T extends Recipe> void register(Identifier id, T recipe) {
        RecipeType type = recipe.getType();
        if (this.registryMap.containsKey(type)) {
            this.registryMap.get(type).register(id, recipe);
            return;
        }

        RecipeRegistry<Recipe> registry = new RecipeRegistry<>();
        registry.register(id, recipe);
        this.registryMap.put(type, registry);
    }

    public Recipe get(Identifier id, RecipeType type) {
        return this.registryMap.get(type).get(id);
    }

    public PagedList<Recipe> getRecipes(RecipeType type, int pageSize, @Nullable Inventory inventory) {
        return this.registryMap.get(type).getRecipes(pageSize, inventory);
    }

    public static RecipeManager get() {
        return RecipeManager.INSTANCE;
    }

    public Collection<Recipe> getRecipes(RecipeType type) {
        return Collections.unmodifiableCollection(this.registryMap.get(type).values());
    }

    public Identifier getKey(RecipeType type, Recipe recipe) {
        return this.registryMap.get(type).getKey(recipe);
    }
}
