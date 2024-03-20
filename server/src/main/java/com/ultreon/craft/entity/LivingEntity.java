package com.ultreon.craft.entity;

import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.events.EntityEvents;
import com.ultreon.craft.events.api.ValueEventResult;
import com.ultreon.craft.server.util.Utils;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class LivingEntity extends Entity {
    public boolean walking;
    public boolean inverseAnim;
    public float walkAnim;
    protected float health;
    private float maxHeath = 20;
    protected boolean isDead = false;
    protected int damageImmunity = 0;

    public float jumpVel = 0.52F;
    public boolean jumping = false;
    public boolean invincible = false;
    protected float oldHealth;
    public float xHeadRot;
    protected float lastDamage;
    protected @Nullable DamageSource lastDamageSource;

    public LivingEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public float getHealth() {
        return this.health;
    }

    public void setHealth(float health) {
        this.health = Mth.clamp(health, 0, this.maxHeath);
    }

    public float getMaxHealth() {
        return this.maxHeath;
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

    public double getSpeed() {
        return this.attributes.get(Attribute.SPEED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method represents the tick behavior for the {@link LivingEntity}.
     */
    @Override
    public void tick() {
        // If the entity is dead, do nothing
        if (this.isDead) return;

        // Handle jumping logic
        if (this.jumping && this.onGround) {
            this.jump();
        }

        // Decrease damage immunity counter
        if (this.damageImmunity > 0) {
            this.damageImmunity--;
        }

        // Check if the entity is in the void and apply damage
        if (this.isInVoid()) {
            this.hurtFromVoid();
        }

        // Check if the entity's health is zero to trigger death event
        if (this.health <= 0) {
            this.health = 0;

            // Trigger entity death event if not already dead and event is not canceled
            if (!this.isDead && !EntityEvents.DEATH.factory().onEntityDeath(this).isCanceled()) {
                this.isDead = true;
                this.onDeath();
            }
        }

        // Call the superclass tick method
        super.tick();
    }

    @Override
    public Vec3d getLookVector() {
        // Calculate the direction vector
        Vec3d direction = new Vec3d();

        this.yRot = Mth.clamp(this.yRot, -89.9F, 89.9F);
        direction.x = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.sin(Math.toRadians(this.xHeadRot)));
        direction.z = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.cos(Math.toRadians(this.xHeadRot)));
        direction.y = (float) (Math.sin(Math.toRadians(this.yRot)));

        // Normalize the direction vector
        direction.nor();
        return direction;
    }

    protected void hurtFromVoid() {
        this.hurt(Integer.MAX_VALUE, DamageSource.VOID);
    }

    public void jump() {
        if (this.isInWater()) return;
        this.velocityY = this.jumpVel;
    }

    /**
     * This method is called when the entity hits the ground.
     * If gravity is enabled and the entity is not in water, it calculates fall damage based on the fall distance.
     * If the calculated damage is greater than 0, it applies that damage to the entity.
     */
    @Override
    protected void hitGround() {
        if (!this.noGravity && !this.isInWater()) {
            int damage = (int) (this.fallDistance - 2.2f);
            if (damage > 0) {
                this.hurt(damage, DamageSource.FALLING);
            }
        }
    }

    /**
     * Inflicts damage on the entity based on the specified amount and source.
     *
     * @param damage the amount of damage to inflict
     * @param source the source of the damage
     */
    public final void hurt(float damage, DamageSource source) {
        // Check if the entity is already dead, has no health, or has temporary invincibility
        if (this.isDead || this.health <= 0 || ((this.invincible || this.damageImmunity > 0) && source.byPassInvincibility()))
            return;

        // Trigger entity damage event
        ValueEventResult<Float> result = EntityEvents.DAMAGE.factory().onEntityDamage(this, source, damage);
        Float value = result.getValue();
        if (value != null) damage = value;

        // Check if custom onHurt behavior should be executed
        if (this.onHurt(damage, source)) return;

        // Play hurt sound if available
        SoundEvent hurtSound = this.getHurtSound();
        if (hurtSound != null) {
            this.world.playSound(hurtSound, this.x, this.y, this.z);
        }

        // Ensure damage is not negative
        damage = Math.max(damage, 0);

        // Update health and damage immunity
        this.oldHealth = this.health;
        this.health = Math.max(this.health - damage, 0);
        this.damageImmunity = 10;

        // Check if entity has died
        if (this.health <= 0) {
            this.health = 0;

            // Trigger entity death event and handle death
            if (!EntityEvents.DEATH.factory().onEntityDeath(this).isCanceled()) {
                this.isDead = true;
                this.onDeath();
            }
        }
    }

    /**
     * Handles {@link #hurt(float, DamageSource)} for subclasses.
     *
     * @param damage the damage dealt.
     * @param source the source of the damage/
     * @return true to cancel the damage.
     */
    @ApiStatus.OverrideOnly
    public boolean onHurt(float damage, DamageSource source) {
        return false;
    }

    /**
     * Get the sound that should be played when the entity gets hurt.
     *
     * @return the hurt sound
     */
    @Nullable
    public SoundEvent getHurtSound() {
        return null;
    }

    /**
     * Called when the entity dies.
     */
    public void onDeath() {
        // Play death sound if available
        SoundEvent deathSound = this.getDeathSound();
        if (deathSound != null) {
            this.world.playSound(deathSound, this.x, this.y, this.z);
        }
    }

    /**
     * Get the sound that should be played when the entity dies.
     *
     * @return the death sound
     */
    @Nullable
    public SoundEvent getDeathSound() {
        return null;
    }

    /**
     * Load the data for the player character.
     *
     * @param data the map containing player data
     */
    @Override
    public void load(MapType data) {
        super.load(data);

        this.health = data.getFloat("health", this.health);
        this.maxHeath = data.getFloat("maxHealth", this.maxHeath);
        this.damageImmunity = data.getInt("damageImmuhghnity", this.damageImmunity);
        this.isDead = data.getBoolean("isDead", this.isDead);
        this.jumpVel = data.getFloat("jumpVelocity", this.jumpVel);
        this.jumping = data.getBoolean("jumping", this.jumping);
        this.invincible = data.getBoolean("invincible", this.invincible);
    }

    /**
     * A description of the entire Java function.
     *
     * @param data Description of the data parameter
     * @return Description of the return value
     */
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

    /**
     * Check if the entity is dead.
     *
     * @return {@code true} if the entity is dead, {@code false} otherwise
     */
    public boolean isDead() {
        return this.isDead;
    }

    /**
     * Get the chunk position of the entity.
     *
     * @return the chunk position
     */
    public ChunkPos getChunkPos() {
        return Utils.toChunkPos(this.getBlockPos());
    }

    /**
     * Instantly kill the entity.
     */
    public void kill() {
        // Set health to zero and mark as dead. Which basically is insta-kill.
        this.lastDamage = this.health;
        this.lastDamageSource = DamageSource.KILL;
        this.health = 0;
        this.isDead = true;

        // Trigger entity death event and handle death
        this.onDeath();
    }

    public boolean isWalking() {
        return this.walking;
    }
}
