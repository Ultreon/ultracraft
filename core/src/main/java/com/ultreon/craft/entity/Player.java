package com.ultreon.craft.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.world.World;

public class Player extends Entity {
    private final PlayerInput input = UltreonCraft.get().playerInput;
    private boolean running;
    private float speed = .5F;
    private float runningSpeed = 1.5F;
    private boolean flying;

    public Player(EntityType<? extends Player> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        this.jumping = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        super.tick();

        setRunning(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT));
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            flying = noGravity = !flying;
        }

        var speed = isRunning() ? getRunningSpeed() : getSpeed();

        Vector3 tmp = new Vector3();
        Vector3 position = getPosition();

        input.tick();

        if (input.forward) {
            tmp.set(getLookVector());
            tmp.y = 0;
            tmp.nor().scl(speed);
            position.add(tmp);
        }
        if (input.backward) {
            tmp.set(getLookVector());
            tmp.y = 0;
            tmp.nor().scl(-speed);
            position.add(tmp);
        }
        if (input.strafeLeft) {
            tmp.set(getLookVector()).crs(0,1, 0).nor().scl(-speed);
            position.add(tmp);
        }
        if (input.strafeRight) {
            tmp.set(getLookVector()).crs(0,1, 0).nor().scl(speed);
            position.add(tmp);
        }
        if (flying) {
            if (input.up) {
                tmp.set(0, 1, 0).nor().scl(speed);
                position.add(tmp);
            }
            if (input.down) {
                tmp.set(0, 1, 0).nor().scl(-speed);
                position.add(tmp);
            }
        }

        this.setPosition(position);
    }

    public float getEyeHeight() {
        return 1.63F;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public float getRunningSpeed() {
        return runningSpeed;
    }

    public void setRunningSpeed(float runningSpeed) {
        this.runningSpeed = runningSpeed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean getFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.noGravity = this.flying = flying;
    }
}
