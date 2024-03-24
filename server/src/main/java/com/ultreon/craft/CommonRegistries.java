package com.ultreon.craft;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.block.entity.BlockEntityTypes;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.recipe.CraftingRecipes;
import com.ultreon.craft.server.GameCommands;
import com.ultreon.craft.sound.event.SoundEvents;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;

public class CommonRegistries {
    public static void registerGameStuff() {
        Blocks.nopInit();
        BlockEntityTypes.register();
        Items.nopInit();
        NoiseConfigs.nopInit();
        EntityTypes.nopInit();
        SoundEvents.nopInit();

        GameCommands.register();

        CraftingRecipes.init();
    }
}
