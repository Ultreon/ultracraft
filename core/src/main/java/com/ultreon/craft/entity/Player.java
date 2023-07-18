package com.ultreon.craft.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.init.Sounds;
import com.ultreon.craft.input.GameInput;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.input.util.ControllerButton;
import com.ultreon.craft.render.gui.screens.DeathScreen;
import com.ultreon.craft.util.Utils;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;

public class Player extends LivingEntity {
    public static Item[] allowed = new Item[]{Items.GRASS_BLOCK, Items.DIRT, Items.SAND, Items.STONE, Items.COBBLESTONE, Items.WATER, Items.WOODEN_PICKAXE};
    public int selected;
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
    }

    public void selectBlock(int i) {
        int toSelect = i % 9;
        if (toSelect < 0) toSelect += 9;
        this.selected = toSelect;
    }

    public Item getSelectedItem() {
        if (this.selected < 0) this.selected = 0;
        return this.selected >= allowed.length ? Items.AIR : allowed[this.selected];
    }

    @Override
    public void tick() {
        super.tick();

        this.jumping = !this.isDead() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched() || GameInput.isControllerButtonDown(ControllerButton.A));

        if (this.topView) {
            this.noGravity = true;
            this.flying = true;
            this.spectating = true;
        }

        if (this.isInVoid() && !this.isDead()) {
            GameInput.startVibration(200, 1.0F);
        }

    }

    @Override
    public boolean onAttack(float damage, DamageSource source) {
        if (source == DamageSource.FALLING) {
            GameInput.startVibration(50, 1.0F);
        }

        return false;
    }

    public boolean isInWater() {
        return this.world.get(this.blockPosition()) == Blocks.WATER;
    }

    public ChunkPos getChunkPos() {
        return Utils.chunkPosFromBlockCoords(this.blockPosition());
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
    public void onDeath() {
        super.onDeath();

        UltreonCraft.get().showScreen(new DeathScreen());
    }

    @Override
    public SoundEvent getHurtSound() {
        return Sounds.PlAYER_HURT;
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
}
