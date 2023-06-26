package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;

public class PlayerInput {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean up;
    public boolean down;
    public float moveX; // PLATFORM: Mobile
    public float moveZ; // PLATFORM: Mobile
    private Vector3 vel = new Vector3();
    private final Vector3 tmp = new Vector3();
    private final UltreonCraft game;

    public PlayerInput(UltreonCraft game) {
        this.game = game;
    }

    public void tick(float speed) {
        Player player = this.game.player;
        if (player == null) return;

        this.vel.set(0, 0, 0);

        this.moveX = 0;
        this.moveZ = 0;

        if (this.game.input.isControllerConnected()) {
            Vector2 joystick = this.game.input.getJoystick(JoystickType.LEFT);
            if (joystick != null) {
                this.moveX = joystick.x * speed * 15;
                this.moveZ = joystick.y * speed * 15;
            } else {
                this.moveX = 0;
                this.moveZ = 0;
            }

            this.tmp.set(-speed * this.moveX, 0, speed * this.moveZ).rotate(player.getXRot(), 0, 1, 0);
            this.vel.set(this.tmp);
        } else if (GamePlatform.instance.hasKeyInput()) {
            this.forward = Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isCursorCatched();
            this.backward = Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isCursorCatched();
            this.strafeLeft = Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isCursorCatched();
            this.strafeRight = Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isCursorCatched();
            this.up = Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched();
            this.down = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isCursorCatched();

            if (this.forward || this.backward || this.strafeLeft || this.strafeRight) {
                if (this.forward) {
                    this.moveZ += speed;
                }
                if (this.backward) {
                    this.moveZ -= speed;
                }
                if (this.strafeLeft) {
                    this.moveX -= speed;
                }
                if (this.strafeRight) {
                    this.moveX += speed;
                }
            }
        } else if (GamePlatform.instance.isMobile()) {
            Vector2 joyStick = this.game.hud.getJoyStick();
            if (joyStick != null) {
                this.moveX = joyStick.x * speed * 15;
                this.moveZ = joyStick.y * speed * 15;
            } else {
                this.moveX = 0;
                this.moveZ = 0;
            }

            this.tmp.set(-speed * this.moveX, 0, speed * this.moveZ).rotate(player.getXRot(), 0, 1, 0);
            this.vel.set(this.tmp);
        }
    }

    private void setVel(Vector3 vel) {
        this.vel = vel;
    }

    public Vector3 getVel() {
        return this.vel;
    }
}
