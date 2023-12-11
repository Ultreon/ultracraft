package com.ultreon.craft.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.util.JoystickType;
import com.ultreon.craft.client.util.Utils;
import com.ultreon.craft.entity.Player;
import com.ultreon.libs.commons.v0.vector.Vec3d;

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
    private int flyCountdown = 0;


    public PlayerInput(UltracraftClient client) {
        this.client = client;
    }

    public void tick(float speed) {
        Player player = this.client.player;
        if (player == null) return;

        this.vel.set(0, 0, 0);

        this.moveX = 0;
        this.moveY = 0;

        this.forward = KeyBinds.walkForwardsKey.isPressed() && Gdx.input.isCursorCatched();
        this.backward = KeyBinds.walkBackwardsKey.isPressed() && Gdx.input.isCursorCatched();
        this.strafeLeft = KeyBinds.walkLeftKey.isPressed() && Gdx.input.isCursorCatched();
        this.strafeRight = KeyBinds.walkRightKey.isPressed() && Gdx.input.isCursorCatched();
        this.up = KeyBinds.jumpKey.isPressed() && Gdx.input.isCursorCatched();
        this.down = KeyBinds.crouchKey.isPressed() && Gdx.input.isCursorCatched();
        player.setRunning(KeyBinds.runningKey.isPressed() && Gdx.input.isCursorCatched());

        if (this.flyCountdown > 0) {
            this.flyCountdown--;

            if (KeyBinds.jumpKey.isJustPressed() && player.isAllowFlight()) {
                player.setFlying(!player.isFlying());
            }
        } else if (KeyBinds.jumpKey.isJustPressed() && player.isAllowFlight()) {
            this.flyCountdown = 10;
        }


        this.move();

        this.controllerMove();

        if (this.moveX > 0)
            player.xRot = Math.max(player.xRot - 45 / (player.xHeadRot - player.xRot + 50), player.xRot - 90);
        else if (this.moveX < -0)
            player.xRot = Math.min(player.xRot + 45 / (player.xRot - player.xHeadRot + 50), player.xRot + 90);
        else if (this.moveY != 0 && player.xRot > player.xHeadRot)
            player.xRot = Math.max(player.xRot - (45 / (player.xRot - player.xHeadRot)), player.xHeadRot);
        else if (this.moveY != 0 && player.xRot < player.xHeadRot)
            player.xRot = Math.min(player.xRot + (45 / (player.xHeadRot - player.xRot)), player.xHeadRot);

        this.tmp.set(-speed * this.moveX, 0, speed * this.moveY).rotate(player.xHeadRot, 0, 1, 0);
        this.vel.set(this.tmp);
    }

    private void controllerMove() {
        if (!this.client.input.isControllerConnected() || this.moveX != 0 || this.moveY != 0)
            return;

        Vector2 joystick = GameInput.getJoystick(JoystickType.LEFT);

        if (joystick == null)
            return;

        this.moveX = -joystick.x;
        this.moveY = joystick.y;
    }

    private void move() {
        if (!this.forward && !this.backward && !this.strafeLeft && !this.strafeRight)
            return;

        if (this.forward)
            this.moveY += 1;

        if (this.backward)
            this.moveY -= 1;

        if (this.strafeLeft)
            this.moveX -= 1;

        if (this.strafeRight)
            this.moveX += 1;
    }

    public Vec3d getVel() {
        return Utils.toCoreLibs(this.vel);
    }

    public Vector3 getVelocity() {
        return this.vel;
    }

    public boolean isWalking() {
        return this.forward || this.backward || this.strafeLeft || this.strafeRight;
    }
}
