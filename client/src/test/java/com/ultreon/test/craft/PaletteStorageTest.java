package com.ultreon.test.craft;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class PaletteStorageTest {
    @Test
    void readWriteTestDual() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.BLOCK);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.ITEM);
        PaletteStorage<Block> blocks = new PaletteStorage<>(Blocks.AIR, 4096);

        for (int i = 0; i < 2048; i++) {
            blocks.set(i, Blocks.ERROR);
        }

        for (int i = 2048; i < 4096; i++) {
            blocks.set(i, Blocks.DIRT);
        }

        save(blocks, "test_data_pre1.ubo");

        for (int i = 0; i < 2048; i++) Assertions.assertEquals(Blocks.ERROR, blocks.get(i));
        for (int i = 2048; i < 4096; i++) Assertions.assertEquals(Blocks.DIRT, blocks.get(i));

        for (int i = 0; i < 2048; i++) {
            blocks.set(i, Blocks.STONE);
        }

        for (int i = 2048; i < 4096; i++) {
            blocks.set(i, Blocks.DIRT);
        }

        save(blocks, "test_data_pre2.ubo");

        for (int i = 0; i < 2048; i++) Assertions.assertEquals(Blocks.STONE, blocks.get(i));
        for (int i = 2048; i < 4096; i++) Assertions.assertEquals(Blocks.DIRT, blocks.get(i));

        for (int i = 2048; i < 4096; i++) {
            blocks.set(i, Blocks.WATER);
        }

        save(blocks, "test_data_pre3.ubo");

        for (int i = 0; i < 2048; i++) Assertions.assertEquals(Blocks.STONE, blocks.get(i));
        for (int i = 2048; i < 4096; i++) Assertions.assertEquals(Blocks.WATER, blocks.get(i));

        @NotNull MapType saved = save(blocks, "test_data.ubo");

        PaletteStorage<Block> newStorage = new PaletteStorage<>(Blocks.AIR, 4096);
        newStorage.load(saved, Block::load);
        Assertions.assertEquals(blocks, newStorage);
    }

    @Test
    void readWriteTestTrio() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.BLOCK);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.ITEM);
        PaletteStorage<Block> blocks = new PaletteStorage<>(Blocks.AIR, 12);

        for (int i = 0; i < 6; i++) {
            blocks.set(i, Blocks.ERROR);
        }

        for (int i = 6; i < 12; i++) {
            blocks.set(i, Blocks.DIRT);
        }

        save(blocks, "test_data_trio_pre1.ubo");

        for (int i = 0; i < 6; i++) Assertions.assertEquals(Blocks.ERROR, blocks.get(i));
        for (int i = 6; i < 12; i++) Assertions.assertEquals(Blocks.DIRT, blocks.get(i));

        for (int i = 0; i < 6; i++) blocks.set(i, Blocks.STONE);
        for (int i = 6; i < 12; i++) blocks.set(i, Blocks.DIRT);
        for (int i = 3; i < 9; i++) blocks.set(i, Blocks.SAND);

        save(blocks, "test_data_trio_pre2.ubo");

        for (int i = 0; i < 3; i++) Assertions.assertEquals(Blocks.STONE, blocks.get(i));
        for (int i = 9; i < 12; i++) Assertions.assertEquals(Blocks.DIRT, blocks.get(i));
        for (int i = 3; i < 9; i++) Assertions.assertEquals(Blocks.SAND, blocks.get(i));

        for (int i = 6; i < 12; i++) blocks.set(i, Blocks.WATER);
        for (int i = 3; i < 9; i++) blocks.set(i, Blocks.SAND);

        save(blocks, "test_data_trio_pre3.ubo");

        for (int i = 0; i < 3; i++) Assertions.assertEquals(Blocks.STONE, blocks.get(i));
        for (int i = 6; i < 9; i++) Assertions.assertEquals(Blocks.SAND, blocks.get(i));
        for (int i = 9; i < 12; i++) Assertions.assertEquals(Blocks.WATER, blocks.get(i));

        @NotNull MapType saved = save(blocks, "test_data_trio.ubo");

        PaletteStorage<Block> newStorage = new PaletteStorage<>(Blocks.AIR, 4096);
        newStorage.load(saved, Block::load);
        Assertions.assertEquals(blocks, newStorage);
    }

    @Test
    void writeRandom5K() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.BLOCK);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.ITEM);
        PaletteStorage<Block> blocks = new PaletteStorage<>(Blocks.AIR, 5000);

        for (int i = 0; i < 5000; i++) {
            blocks.set(i, Registries.BLOCK.random());
        }

        @NotNull MapType saved = save(blocks, "test_data_write_random.ubo");

        PaletteStorage<Block> newStorage = new PaletteStorage<>(Blocks.AIR, 4096);
        newStorage.load(saved, Block::load);
        Assertions.assertEquals(blocks, newStorage);
    }

    @Test
    void writeRandom5KAndUpdate10x() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.BLOCK);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.ITEM);
        PaletteStorage<Block> blocks = new PaletteStorage<>(Blocks.AIR, 5000);

        for (int i = 0; i < 5000; i++) {
            blocks.set(i, Registries.BLOCK.random());
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5000; j++) {
                blocks.set(j, Registries.BLOCK.random());
            }
        }

        @NotNull MapType saved = save(blocks, "test_data_update_random.ubo");

        PaletteStorage<Block> newStorage = new PaletteStorage<>(Blocks.AIR, 5000);
        newStorage.load(saved, Block::load);
        Assertions.assertEquals(blocks, newStorage);
    }

    @Test
    void writeRandom100KAndUpdate10x() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.BLOCK);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.ITEM);
        PaletteStorage<Block> blocks = new PaletteStorage<>(Blocks.AIR, 100000);

        for (int i = 0; i < 100000; i++) {
            blocks.set(i, Registries.BLOCK.random());
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 100000; j++) {
                blocks.set(j, Registries.BLOCK.random());
            }
        }

        @NotNull MapType saved = save(blocks, "test_data_update_random_100k.ubo");

        PaletteStorage<Block> newStorage = new PaletteStorage<>(Blocks.AIR, 100000);
        newStorage.load(saved, Block::load);
        Assertions.assertEquals(blocks, newStorage);
    }

    @Test
    void write100KAndUpdateLastSingle() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.BLOCK);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.ITEM);
        PaletteStorage<Block> blocks = new PaletteStorage<>(Blocks.AIR, 100000);

        for (int i = 0; i < 100000; i++) {
            blocks.set(i, Blocks.SAND);
        }

        blocks.set(25565, Blocks.STONE);
        blocks.set(25565, Blocks.WATER);
        blocks.set(25565, Blocks.DIRT);
        blocks.set(25565, Blocks.CACTUS);

        @NotNull MapType saved = save(blocks, "test_data_update_random_100k.ubo");

        PaletteStorage<Block> newStorage = new PaletteStorage<>(Blocks.AIR, 100000);
        newStorage.load(saved, Block::load);
        Assertions.assertEquals(blocks, newStorage);

        Assertions.assertEquals(Blocks.CACTUS, blocks.get(25565), "Integrity check failed at 25565");

        for (int i = 0; i < 100000; i++) {
            if (i == 25565) continue;
            int finalI = i;
            Assertions.assertEquals(Blocks.SAND, blocks.get(i), () -> "Integrity check failed at " + finalI);
        }
    }

    @Test
    void write100KAndUpdateLastSingleWithExisting() {
        Blocks.nopInit();
        Items.nopInit();
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.BLOCK);
        RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(CommonConstants.NAMESPACE, Registries.ITEM);
        PaletteStorage<Block> blocks = new PaletteStorage<>(Blocks.AIR, 100000);

        for (int i = 0; i < 100000; i++) {
            blocks.set(i, Blocks.SAND);
        }

        blocks.set(25565, Blocks.STONE);
        blocks.set(25565, Blocks.WATER);
        blocks.set(25565, Blocks.DIRT);
        blocks.set(25565, Blocks.CACTUS);
        blocks.set(25565, Blocks.SAND);

        @NotNull MapType saved = save(blocks, "test_data_update_random_100k.ubo");

        PaletteStorage<Block> newStorage = new PaletteStorage<>(Blocks.AIR, 100000);
        newStorage.load(saved, Block::load);
        Assertions.assertEquals(blocks, newStorage);

        for (int i = 0; i < 100000; i++) {
            if (i == 25565) continue;
            int finalI = i;
            Assertions.assertEquals(Blocks.SAND, blocks.get(i), () -> "Integrity check failed at " + finalI);
        }
    }

    @NotNull
    private static MapType save(PaletteStorage<Block> blocks, String path) {
        MapType saved = blocks.save(new MapType(), (block) -> {
            MapType mapType = new MapType();
            mapType.putString("id", String.valueOf(block.getId()));
            return mapType;
        });
        try {
            DataIo.writeCompressed(saved, new File(path));
            String uso = DataIo.toUso(saved);
            Files.write(new File(path.replace(".ubo", ".uso")).toPath(), uso.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return saved;
    }
}
