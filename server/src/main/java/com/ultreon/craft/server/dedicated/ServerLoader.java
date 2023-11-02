package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;

public class ServerLoader {
    public void load() {
        Registries.init();
        Blocks.nopInit();
        Items.nopInit();
        NoiseSettingsInit.nopInit();
        EntityTypes.nopInit();
    }
}
