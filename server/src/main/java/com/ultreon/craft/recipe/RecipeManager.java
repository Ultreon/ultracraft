package com.ultreon.craft.recipe;

import com.badlogic.gdx.utils.IdentityMap;
import com.ultreon.craft.events.LoadingEvent;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.resources.ResourceCategory;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.resources.StaticResource;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.util.PagedList;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecipeManager {
    private static final RecipeManager INSTANCE = new RecipeManager();
    private final IdentityMap<RecipeType<?>, RecipeRegistry<Recipe>> registryMap = new IdentityMap<>();

    private RecipeManager() { }

    public void load(UltracraftServer server) {
        ResourceManager resourceManager = server.getResourceManager();
        List<ResourceCategory> resourceCategory = resourceManager.getResourceCategory(RecipeRegistry.CATEGORY);
        for (ResourceCategory cat : resourceCategory) {
            for (StaticResource resource : cat.resources()) {
                try {
                    Recipe recipe = RecipeManager.loadRecipe(resource);
                    if (recipe != null) {
                        this.register(resource.id(), recipe);
                    }
                } catch (Exception e) {
                    UltracraftServer.LOGGER.error("Failed to load recipe: " + resource.id().mapPath(path -> {
                        String prefix = RecipeRegistry.CATEGORY + "/";
                        if (path.startsWith(prefix)) {
                            return path.substring(prefix.length());
                        }

                        return path;
                    }), e);
                }
            }
        }
    }

    private static Recipe loadRecipe(StaticResource resource) {
        Identifier id = resource.id();
        Json5Element json5Element = resource.readJson5();

        if (json5Element == null) {
            UltracraftServer.LOGGER.warn("Failed to load recipe as it's unreadable: " + id);
            return null;
        }

        if (!json5Element.isJson5Object()) {
            UltracraftServer.LOGGER.warn("Failed to load recipe as it is not an object: " + id);
            return null;
        }

        Json5Object root = json5Element.getAsJson5Object();

        Json5Element typeElement = root.get("type");
        if (!typeElement.isJson5Primitive() && !typeElement.getAsJson5Primitive().isString()) {
            UltracraftServer.LOGGER.warn("Failed to load recipe as it is not a string: " + id);
            return null;
        }

        String type = typeElement.getAsJson5Primitive().getAsString();
        Identifier recipeTypeId = new Identifier(type);

        RecipeType<?> recipeType = Registries.RECIPE_TYPE.get(recipeTypeId);
        if (recipeType == null) {
            UltracraftServer.LOGGER.warn("Failed to load recipe as it has an invalid type: " + id);
            return null;
        }

        return recipeType.deserialize(id, root);
    }

    public <T extends Recipe> void register(Identifier id, T recipe) {
        RecipeType<?> type = recipe.getType();
        if (this.registryMap.containsKey(type)) {
            this.registryMap.get(type).register(id, recipe);
            return;
        }

        RecipeRegistry<Recipe> registry = new RecipeRegistry<>();
        registry.register(id, recipe);
        this.registryMap.put(type, registry);
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe> @Nullable T get(Identifier id, RecipeType<T> type) {
        return (T) this.registryMap.get(type).get(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe> PagedList<? extends T> getRecipes(RecipeType<T> type, int pageSize, @Nullable Inventory inventory) {
        return (PagedList<? extends T>) this.registryMap.get(type).getRecipes(pageSize, inventory);
    }

    public static RecipeManager get() {
        return RecipeManager.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe> Collection<T> getRecipes(RecipeType<T> type) {
        return (Collection<T>) Collections.unmodifiableCollection(this.registryMap.get(type).values());
    }

    public Identifier getKey(RecipeType<?> type, Recipe recipe) {
        return this.registryMap.get(type).getKey(recipe);
    }

    public void fireRecipeModifications() {
        for (RecipeType<?> type : Registries.RECIPE_TYPE.values()) {
            LoadingEvent.MODIFY_RECIPES.factory().onModifyRecipes(this, type, this.registryMap.get(type));
        }
    }

    public void freeze() {
        for (RecipeType<?> type : Registries.RECIPE_TYPE.values()) {
            this.registryMap.get(type).freeze();
        }
    }
}
