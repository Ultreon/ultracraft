package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.input.util.JoystickType;

public class PlayerInput {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean up;
    public boolean down;
    public float moveX;
    public float moveY;
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
        this.moveY = 0;

        if (GamePlatform.instance.hasKeyInput()) {
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
        } else if (GamePlatform.instance.isMobile()) {
            Vector2 joyStick = this.game.hud.getJoyStick();
            if (joyStick != null) {
                this.moveX = joyStick.x;
                this.moveY = joyStick.y;
            }
        }

        if (this.game.input.isControllerConnected() && this.moveX == 0 && this.moveY == 0) {
            Vector2 joystick = GameInput.getJoystick(JoystickType.LEFT);
            if (joystick != null) {
                this.moveX = joystick.x;
                this.moveY = joystick.y;
            }
        }

        this.tmp.set(-speed * this.moveX, 0, speed * this.moveY).rotate(player.getXRot(), 0, 1, 0);
        this.vel.set(this.tmp);
    }

    @Deprecated(forRemoval = true)
    private void setVel(Vector3 vel) {
        this.vel = vel;
    }

    public Vector3 getVel() {
        return this.vel;
    }
}
