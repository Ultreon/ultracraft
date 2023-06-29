package com.ultreon.craft.entity;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.events.EntityEvents;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.events.v1.ValueEventResult;

public class LivingEntity extends Entity {
    private float health;
    private float maxHeath = 20;
    private boolean isDead = false;
    private int damageImmunity = 0;

    public float jumpVel = 0.55F;
    public boolean jumping = false;
    public boolean invincible = false;

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

    public float getJumpVel() {
        return this.jumpVel;
    }

    public void setJumpVel(float jumpVel) {
        this.jumpVel = jumpVel;
    }

    public boolean isInvincible() {
        return this.invincible;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isDead) return;

        if (this.jumping && this.onGround) {
            this.jump();
        }

        if (damageImmunity > 0) {
            damageImmunity--;
        }

        if (this.isInVoid()) {
            this.hurt(5, DamageSource.VOID);
        }

        if (this.health <= 0) {
            this.health = 0;

            if (!this.isDead) {
                if (!EntityEvents.DEATH.factory().onEntityDeath(this).isCanceled()) {
                    this.isDead = true;
                    this.onDeath();
                }
            }
        }
    }

    public void jump() {
        this.velocityY = this.jumpVel;
    }

    @Override
    protected void hitGround() {
        if (!this.noGravity && this.fallDistance > 4.5F) {
            float damage = this.fallDistance - 4.5F;
            if (damage > 0) {
                this.hurt(damage, DamageSource.FALLING);
            }
        }
    }

    public final void hurt(float damage, DamageSource source) {
        if (this.isDead || this.health <= 0 || this.invincible || this.damageImmunity > 0) return;

        ValueEventResult<Float> result = EntityEvents.DAMAGE.factory().onEntityDamage(this, source, damage);
        Float value = result.getValue();
        if (value != null) damage = value;

        if (this.onAttack(damage, source)) return;

        SoundEvent hurtSound = getHurtSound();
        if (hurtSound != null) {
            UltreonCraft.get().playSound(hurtSound);
        }

        damage = Math.max(damage, 0);

        health = Math.max(health - damage, 0);
        damageImmunity = 10;

        if (this.health <= 0) {
            this.health = 0;

            if (!EntityEvents.DEATH.factory().onEntityDeath(this).isCanceled()) {
                this.isDead = true;
                this.onDeath();
            }
        }
    }

    public boolean onAttack(float damage, DamageSource source) {
        return false;
    }

    public SoundEvent getHurtSound() {
        return null;
    }

    public void onDeath() {

    }

    @Override
    public void load(MapType data) {
        super.load(data);

        this.health = data.getFloat("health", this.health);
        this.maxHeath = data.getFloat("maxHealth", this.maxHeath);
        this.damageImmunity = data.getInt("damageImmunity", this.damageImmunity);
        this.isDead = data.getBoolean("isDead", this.isDead);
        this.jumpVel = data.getFloat("jumpVelocity", this.jumpVel);
        this.jumping = data.getBoolean("jumping", this.jumping);
        this.invincible = data.getBoolean("invincible", this.invincible);
    }

    @Override
    public MapType save(MapType data) {
        data = super.save(data);

        data.putFloat("health", this.health);
        data.putFloat("maxHealth", this.maxHeath);
        data.putInt("damageImmunity", this.damageImmunity);
        data.putBoolean("isDead", this.isDead);
        data.putFloat("jumpVelocity", this.jumpVel);
        data.putBoolean("jumping", this.jumping);
        data.putBoolean("jumping", this.invincible);

        return data;
    }

    public boolean isDead() {
        return this.isDead;
    }
}
