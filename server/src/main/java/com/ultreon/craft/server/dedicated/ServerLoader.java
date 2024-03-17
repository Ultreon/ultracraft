package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.CommonLoader;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.GameCommands;
import com.ultreon.craft.world.gen.biome.Biomes;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import net.fabricmc.loader.api.FabricLoader;

/**
 * This class is responsible for loading the server configurations and initializing various components.
 */
public class ServerLoader {

    /**
     * Loads server configurations and initializes various components.
     */
    public void load() {
        // Initialize configuration entry points
        CommonLoader.initConfigEntrypoints(FabricLoader.getInstance());

        // Initialize registries
        Registries.nopInit();
        Blocks.nopInit();
        Items.nopInit();
        NoiseConfigs.nopInit();
        EntityTypes.nopInit();
        Biomes.nopInit();

        // Register game commands
        GameCommands.register();
    }
}
