package com.ultreon.craft.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.init.Sounds;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.render.gui.screens.DeathScreen;
import com.ultreon.craft.util.Utils;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;

public class Player extends LivingEntity {
    private final PlayerInput input = UltreonCraft.get().playerInput;
    public static final Block[] ALLOWED = new Block[]{Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.STONE, Blocks.WATER};
    public int selected;
    private boolean running;
    private float walkingSpeed = .05F;
    private float flyingSpeed = 0.5F;
    public final float runModifier = 1.75F;
    public final float crouchModifier = 0.5F;
    private boolean flying;
    private boolean crouching;
    private boolean spectating;
    // TODO: @DEBUG START
    public final boolean topView = false;
    // TODO: @DEBUG END

    public Player(EntityType<? extends Player> entityType, World world) {
        super(entityType, world);
    }

    public void selectBlock(int i) {
        selected = i % 9;
    }

    public Block getSelectedBlock() {
        return this.selected >= ALLOWED.length ? Blocks.AIR : ALLOWED[this.selected];

    }

    @Override
    public void tick() {
        this.jumping = Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched();

        if (this.topView) {
            this.noGravity = true;
            this.flying = true;
            this.spectating = true;
        }
        super.tick();

        setRunning(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isCursorCatched());

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) flying = noGravity = !flying;
        if (!flying) crouching = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        float speed;
        if (flying) speed = this.flyingSpeed;
        else speed = this.walkingSpeed;

        if (isCrouching()) speed *= crouchModifier;
        else if (isRunning()) speed *= runModifier;

        if (!this.topView) {
            Vector3 tmp = new Vector3();
            Vector3 vel = getVelocity();

            input.tick();

            if (input.forward) {
                tmp.set(getLookVector());
                tmp.y = 0;
                tmp.nor().scl(speed);
                vel.add(tmp);
            }
            if (input.backward) {
                tmp.set(getLookVector());
                tmp.y = 0;
                tmp.nor().scl(-speed);
                vel.add(tmp);
            }
            if (input.strafeLeft) {
                tmp.set(getLookVector()).crs(0, 1, 0).nor().scl(-speed);
                vel.add(tmp);
            }
            if (input.strafeRight) {
                tmp.set(getLookVector()).crs(0, 1, 0).nor().scl(speed);
                vel.add(tmp);
            }
            if (isInWater() && input.up) {
                tmp.set(0, 1, 0).nor().scl(speed);
                vel.add(tmp);
            }
            if (flying) {
                if (input.up) {
                    tmp.set(0, 1, 0).nor().scl(speed);
                    vel.add(tmp);
                }
                if (input.down) {
                    tmp.set(0, 1, 0).nor().scl(-speed);
                    vel.add(tmp);
                }
            }

            this.setVelocity(vel);
        } else {
            this.x = this.z = 0;
            this.y = 120;
            this.xRot = 45;
            this.yRot =  -45;
        }
    }

    private boolean isInWater() {
        return world.get(blockPosition()) == Blocks.WATER;
    }

    public ChunkPos getChunkPos() {
        return Utils.chunkPosFromBlockCoords(getPosition());
    }

    public float getEyeHeight() {
        return crouching ? 1.15F : 1.63F;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public float getWalkingSpeed() {
        return walkingSpeed;
    }

    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    public float getFlyingSpeed() {
        return flyingSpeed;
    }

    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.noGravity = this.flying = flying;
    }

    public boolean isCrouching() {
        return crouching;
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public boolean isSpectating() {
        return spectating;
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
}
