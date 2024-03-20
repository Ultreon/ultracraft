package com.ultreon.craft.entity;

import com.ultreon.craft.world.World;

public class Something extends LivingEntity {
    public Something(EntityType<? extends Something> entityType, World world) {
        super(entityType, world);
    }
}
