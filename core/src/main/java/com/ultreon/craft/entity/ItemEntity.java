package com.ultreon.craft.entity;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public class ItemEntity extends Entity {
    private Block item = Blocks.AIR;

    public ItemEntity(EntityType<? extends ItemEntity> entityType, World world, Block item) {
        this(entityType, world, item, new Vec3d());
    }

    public ItemEntity(EntityType<? extends ItemEntity> entityType, World world, Block item, Vec3d velocity) {
        super(entityType, world);
        this.item = item;
        this.setVelocity(velocity);
    }

    public ItemEntity(EntityType<? extends ItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.item.isAir()) {
            this.deferDeletion();
        }
    }

    public float getSpin() {
        return 2F * this.getAge();
    }

    public Block getItem() {
        return this.item;
    }

    public void setItem(Block item) {
        this.item = item;
    }
}
