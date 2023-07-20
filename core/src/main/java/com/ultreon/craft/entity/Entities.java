package com.ultreon.craft.entity;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class Entities {
    public static final EntityType<Player> PLAYER = register("player", new EntityType.Builder<Player>().size(0.6f, 1.9f).factory(Player::new));
    public static final EntityType<ItemEntity> ITEM = register("item", new EntityType.Builder<ItemEntity>().size(0.4f, 0.4f).factory(ItemEntity::new));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build();
        Registries.ENTITIES.register(new Identifier(UltreonCraft.NAMESPACE, name), entityType);
        return entityType;
    }

    public static void nopInit() {

    }
}
