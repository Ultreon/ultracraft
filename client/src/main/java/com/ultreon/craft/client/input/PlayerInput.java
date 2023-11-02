package com.ultreon.craft.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.util.JoystickType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.client.util.Utils;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.ApiStatus;

public class PlayerInput {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean up;
    public boolean down;
    public float moveX;
    public float moveY;
    private final Vector3 vel = new Vector3();
    private final Vector3 tmp = new Vector3();
    private final UltracraftClient client;

    public PlayerInput(UltracraftClient client) {
        this.client = client;
    }

    public void tick(float speed) {
        Player player = this.client.player;
        if (player == null) return;

        this.vel.set(0, 0, 0);

        this.moveX = 0;
        this.moveY = 0;

        this.forward = Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isCursorCatched();
        this.backward = Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isCursorCatched();
        this.strafeLeft = Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isCursorCatched();
        this.strafeRight = Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isCursorCatched();
        this.up = Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched();
        this.down = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isCursorCatched();

        if (this.forward || this.backward || this.strafeLeft || this.strafeRight) {
            if (this.forward) {
                this.moveY += 1;
            }
            if (this.backward) {
                this.moveY -= 1;
            }
            if (this.strafeLeft) {
                this.moveX -= 1;
            }
            if (this.strafeRight) {
                this.moveX += 1;
            }
        }

        if (this.client.input.isControllerConnected() && this.moveX == 0 && this.moveY == 0) {
            Vector2 joystick = GameInput.getJoystick(JoystickType.LEFT);
            if (joystick != null) {
                this.moveX = -joystick.x;
                this.moveY = joystick.y;
            }
        }

        this.tmp.set(-speed * this.moveX, 0, speed * this.moveY).rotate(player.getXRot(), 0, 1, 0);
        this.vel.set(this.tmp);
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    private void setVel(Vector3 vel) {
        this.vel.set(vel);
    }

    public Vec3d getVel() {
        return Utils.toCoreLibs(this.vel);
    }

    public Vector3 getVelocity() {
        return this.vel;
    }
}
