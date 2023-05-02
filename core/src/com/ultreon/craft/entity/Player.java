package com.ultreon.craft.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.util.Vec3i;
import com.ultreon.craft.world.World;

public class Player extends Entity {
    private PlayerInput input = UltreonCraft.get().playerInput;
    private boolean running;
    private float runningSpeed;
    private float speed;

    public Player(EntityType<? extends Player> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        var speed = isRunning() ? getRunningSpeed() : getSpeed();

        Vector3 tmp = new Vector3();
        Vector3 position = getPosition();
        if (input.forward) {
            tmp.set(new Vector3(getRotation(), 0));
            tmp.y = 0;
//            tmp.z = 0;
            tmp.nor().scl(speed);
            position.add(tmp);
        }
        if (input.backward) {
            tmp.set(new Vector3(getRotation(), 0));
            tmp.y = 0;
//            tmp.z = 0;
            tmp.nor().scl(-speed);
            position.add(tmp);
        }
        if (input.strafeLeft) {
            tmp.set(new Vector3(getRotation(), 0)).crs(0,1, 0).nor().scl(-speed);
            position.add(tmp);
        }
        if (input.strafeRight) {
            tmp.set(new Vector3(getRotation(), 0)).crs(0,1, 0).nor().scl(speed);
            position.add(tmp);
        }
        if (input.up) {
            tmp.set(0,1, 0).nor().scl(speed);
            position.add(tmp);
        }
        if (input.down) {
            tmp.set(0,1, 0).nor().scl(-speed);
            position.add(tmp);
        }

        this.setPosition(position);
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
}
