package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.GameCommands;
import com.ultreon.craft.world.gen.biome.Biomes;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;

public class ServerLoader {
    public void load() {
        Registries.nopInit();
        Blocks.nopInit();
        Items.nopInit();
        NoiseConfigs.nopInit();
        EntityTypes.nopInit();

        Biomes.nopInit();

        GameCommands.register();
    }
}
