package com.ultreon.craft.entity;

import com.ultreon.craft.events.PlayerEvents;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.sound.event.SoundEvents;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Ray;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Player extends LivingEntity {
    @ApiStatus.Internal
    public int selected;
    public Inventory inventory = new Inventory();
    private boolean running;
    private float walkingSpeed = .09F;
    private float flyingSpeed = 0.5F;
    public float runModifier = 1.5F;
    public float crouchModifier = 0.5F;
    private boolean flying;
    private boolean crouching;
    private boolean spectating;
    // TODO: @DEBUG START
    public boolean topView = false;
    // TODO: @DEBUG END

    public Player(EntityType<? extends Player> entityType, World world) {
        super(entityType, world);

        this.inventory.build();
        this.setInitialItems();

        this.setFlying(true);
    }

    private void setInitialItems() {
        if (PlayerEvents.INITIAL_ITEMS.factory().onPlayerInitialItems(this, this.inventory).isCanceled()) {
            return;
        }

        this.inventory.getHotbarSlot(0).setItem(Items.WOODEN_PICKAXE.defaultStack());
        this.inventory.getHotbarSlot(1).setItem(Items.WOODEN_SHOVEL.defaultStack());
    }

    public void selectBlock(int i) {
        int toSelect = i % 9;
        if (toSelect < 0) toSelect += 9;
        this.selected = toSelect;
    }

    public ItemStack getSelectedItem() {
        if (this.selected < 0) this.selected = 0;
        return this.selected >= 9 ? ItemStack.empty() : this.inventory.getHotbarSlot(this.selected).getItem();
    }

    @Override
    public void tick() {
        if (this.topView) {
            this.noGravity = true;
            this.flying = true;
            this.spectating = true;
        }

        if (this.jumping && !this.flying) this.swimUp();

        super.tick();
    }

    protected void onVoidDamage() {

    }

    @Override
    public boolean isAffectedByFluid() {
        return !(this.flying || this.noClip) && super.isAffectedByFluid();
    }

    public float getEyeHeight() {
        return this.crouching ? 1.15F : 1.63F;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public void setFlying(boolean flying) {
        this.noGravity = this.flying = flying;
    }

    public boolean isCrouching() {
        return this.crouching;
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public boolean isSpectating() {
        return this.spectating;
    }

    public void setSpectating(boolean spectating) {
        this.spectating = this.noClip = this.noGravity = this.flying = spectating;
    }

    @Override
    public SoundEvent getHurtSound() {
        return SoundEvents.PLAYER_HURT;
    }

    public HitResult rayCast() {
        return this.world.rayCast(new Ray(this.getPosition().add(0, this.getEyeHeight(), 0), this.getLookVector()));
    }

    @Override
    public void load(MapType data) {
        super.load(data);

        this.selected = data.getByte("selectedItem", (byte) this.selected);
        this.flying = data.getBoolean("flying", this.flying);
        this.spectating = data.getBoolean("spectating", this.spectating);
        this.crouching = data.getBoolean("crouching", this.crouching);
        this.running = data.getBoolean("running", this.running);
        this.topView = data.getBoolean("crouching", this.topView);
        this.walkingSpeed = data.getFloat("walkingSpeed", this.walkingSpeed);
        this.flyingSpeed = data.getFloat("flyingSpeed", this.flyingSpeed);
        this.crouchModifier = data.getFloat("crouchingModifier", this.crouchModifier);
        this.runModifier = data.getFloat("runModifier", this.runModifier);
    }

    @Override
    public MapType save(MapType data) {
        data = super.save(data);

        data.putByte("selectedItem", this.selected);
        data.putBoolean("flying", this.flying);
        data.putBoolean("spectating", this.spectating);
        data.putBoolean("crouching", this.crouching);
        data.putBoolean("running", this.running);
        data.putBoolean("crouching", this.topView);
        data.putFloat("walkingSpeed", this.walkingSpeed);
        data.putFloat("flyingSpeed", this.flyingSpeed);
        data.putFloat("crouchingModifier", this.crouchModifier);
        data.putFloat("runModifier", this.runModifier);

        return data;
    }

    public abstract UUID getUuid();

    protected abstract void setUuid(UUID uuid);

    public void playSound(@Nullable SoundEvent sound, float volume) {

    }
}
