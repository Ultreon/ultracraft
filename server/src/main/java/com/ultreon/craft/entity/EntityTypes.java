package com.ultreon.craft.entity;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.libs.commons.v0.Identifier;

public class EntityTypes {
    public static EntityType<Player> PLAYER = EntityTypes.register("player", new EntityType.Builder<Player>().size(0.4f, 1.8f).factory((entityType, world) -> null));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build();
        Registries.ENTITIES.register(new Identifier(UltracraftServer.NAMESPACE, name), entityType);
        return entityType;
    }

    public static void nopInit() {

    }
}
