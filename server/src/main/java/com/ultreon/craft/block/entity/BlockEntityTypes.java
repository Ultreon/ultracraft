package com.ultreon.craft.block.entity;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;

public class BlockEntityTypes {
    public static final BlockEntityType<CrateBlockEntity> CRATE = nopInit("crate", new BlockEntityType<>(CrateBlockEntity::new));

    private static <T extends BlockEntity> BlockEntityType<T> nopInit(String name, BlockEntityType<T> type) {
        Registries.BLOCK_ENTITY_TYPE.register(new Identifier(name), type);
        return type;
    }

    public static void nopInit() {

    }
}
