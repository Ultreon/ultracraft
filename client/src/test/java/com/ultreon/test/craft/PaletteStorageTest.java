package com.ultreon.test.craft;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.registries.v0.event.RegistryEvents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PaletteStorageTest {
    @Test
    void readWriteTest() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(Registries.BLOCKS);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(Registries.ITEMS);
        PaletteStorage<Block> blocks = new PaletteStorage<>(4096);

        for (int i = 0; i < 2048; i++) {
            blocks.set(i, Blocks.ERROR);
        }

        for (int i = 2048; i < 4096; i++) {
            blocks.set(i, Blocks.DIRT);
        }

        for (int i = 0; i < 2048; i++) {
            blocks.set(i, Blocks.STONE);
        }

        for (int i = 2048; i < 4096; i++) {
            blocks.set(i, Blocks.DIRT);
        }

        MapType saved = blocks.save(new MapType(), Block::save);

        PaletteStorage<Block> newStorage = new PaletteStorage<>(4096);
        newStorage.load(saved, Block::load);

        Assertions.assertEquals(blocks, newStorage);
    }
}
