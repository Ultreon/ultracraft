package com.ultreon.craft.entity;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;

public class EntityTypes {
    public static final EntityType<Player> PLAYER = EntityTypes.register("player", new EntityType.Builder<Player>().size(0.4f, 1.8f).factory((entityType, world) -> null));
    public static final EntityType<DroppedItem> DROPPED_ITEM = EntityTypes.register("dropped_item", new EntityType.Builder<DroppedItem>().size(0.25f, 0.25f).factory(DroppedItem::new));
    public static final EntityType<Something> SOMETHING = EntityTypes.register("something", new EntityType.Builder<Something>().size(0.25f, 0.25f).factory(Something::new));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build();
        Registries.ENTITY_TYPE.register(new Identifier(CommonConstants.NAMESPACE, name), entityType);
        return entityType;
    }

    public static void nopInit() {

    }
}
