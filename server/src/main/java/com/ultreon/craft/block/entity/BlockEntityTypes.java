package com.ultreon.craft.block.entity;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;

public class BlockEntityTypes {
    public static final BlockEntityType<CrateBlockEntity> CRATE = register("crate", new BlockEntityType<>(CrateBlockEntity::new));

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        Registries.BLOCK_ENTITY_TYPE.register(new ElementID(name), type);
        return type;
    }

    public static void register() {

    }
}
