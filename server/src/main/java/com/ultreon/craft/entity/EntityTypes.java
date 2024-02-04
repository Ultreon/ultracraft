package com.ultreon.craft.entity;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;

public class EntityTypes {
    public static final EntityType<Player> PLAYER = EntityTypes.register("player", new EntityType.Builder<Player>().size(0.4f, 1.8f).factory((entityType, world) -> null));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build();
        Registries.ENTITY_TYPE.register(new ElementID(CommonConstants.NAMESPACE, name), entityType);
        return entityType;
    }

    public static void nopInit() {

    }
}
