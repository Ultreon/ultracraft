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

        if (GamePlatform.instance.hasKeyInput()) {
            this.forward = Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isCursorCatched();
            this.backward = Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isCursorCatched();
            this.strafeLeft = Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isCursorCatched();
            this.strafeRight = Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isCursorCatched();
            this.up = Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched();
            this.down = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isCursorCatched();

            if (this.forward) {
                this.tmp.set(player.getLookVector());
                this.tmp.y = 0;
                this.tmp.nor().scl(speed);
                this.vel.add(this.tmp);
            }
            if (this.backward) {
                this.tmp.set(player.getLookVector());
                this.tmp.y = 0;
                this.tmp.nor().scl(-speed);
                this.vel.add(this.tmp);
            }
            if (this.strafeLeft) {
                this.tmp.set(player.getLookVector()).crs(0, 1, 0).nor().scl(-speed);
                this.vel.add(this.tmp);
            }
            if (this.strafeRight) {
                this.tmp.set(player.getLookVector()).crs(0, 1, 0).nor().scl(speed);
                this.vel.add(this.tmp);
            }
        } else {
            Vector2 joyStick = this.game.hud.getJoyStick();
            if (joyStick != null) {
                this.moveX = joyStick.x * speed;
                this.moveZ = joyStick.y * speed;
            } else {
                this.moveX = 0;
                this.moveZ = 0;
            }

            this.vel.set(speed * this.moveX, 0, speed * this.moveZ);
        }
    }

    private void setVel(Vector3 vel) {
        this.vel = vel;
    }

    public Vector3 getVel() {
        return this.vel;
    }
}
