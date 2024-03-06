package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.recipe.Recipe;
import com.ultreon.craft.recipe.RecipeManager;
import com.ultreon.craft.recipe.RecipeRegistry;
import com.ultreon.craft.recipe.RecipeType;

public class LoadingEvent {
    public static final Event<RegisterCommands> REGISTER_COMMANDS = Event.create();
    public static final Event<RegisterRecipes> REGISTER_RECIPES = Event.create();
    public static final Event<ModifyRecipes> MODIFY_RECIPES = Event.create();

    @FunctionalInterface
    public interface RegisterCommands {
        void onRegisterCommands();
    }

    @FunctionalInterface
    public interface RegisterRecipes {
        void onLoadRecipes(RecipeManager recipes);
    }

    @FunctionalInterface
    public interface ModifyRecipes {
        void onModifyRecipes(RecipeManager recipes, RecipeType type, RecipeRegistry<Recipe> recipeRecipeRegistry);
    }
}
