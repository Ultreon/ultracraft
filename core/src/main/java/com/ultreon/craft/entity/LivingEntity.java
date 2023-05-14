package com.ultreon.craft.entity;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.world.World;

public class LivingEntity extends Entity {
    private float health;
    private float maxHeath = 20;
    private boolean isDead = false;
    private int damageImmunity = 0;

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

        if (damageImmunity > 0) {
            damageImmunity--;
        }

        if (this.y < World.WORLD_DEPTH - 64) {
            attack(5);
        }

        if (this.health <= 0) {
            this.health = 0;

            if (!this.isDead) {
                this.isDead = true;
                onDeath();
            }
        }
    }

    @Override
    protected void hitGround() {
        if (fallDistance != 0.0F) System.out.println("fallDistance = " + fallDistance);
        if (this.fallDistance > 3) {
            float damage = this.fallDistance - 3;
            this.attack(damage);
        }
    }

    private void attack(float damage) {
        if (isDead) return;
        if (damageImmunity > 0) return;

        SoundEvent hurtSound = getHurtSound();
        if (hurtSound != null) {
            UltreonCraft.get().playSound(hurtSound);
        }

        damage = Math.max(damage, 0);

        health = Math.max(health - damage, 0);
        damageImmunity = 10;
    }

    public SoundEvent getHurtSound() {
        return null;
    }

    public void onDeath() {

    }
}
