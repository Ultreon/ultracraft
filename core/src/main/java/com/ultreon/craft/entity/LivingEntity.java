package com.ultreon.craft.entity;

import com.ultreon.craft.world.World;

public class LivingEntity extends Entity {
    private float health;
    private float maxHeath = 20;
    private boolean isDead = false;

    public LivingEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getMaxHeath() {
        return maxHeath;
    }

    public void setMaxHeath(float maxHeath) {
        this.maxHeath = maxHeath;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.health <= 0) {
            this.health = 0;

            if (!this.isDead) {
                this.isDead = true;
                onDeath();
            }
        }
    }

    public void onDeath() {

    }
}
