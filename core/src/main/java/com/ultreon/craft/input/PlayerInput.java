package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ultreon.craft.UltreonCraft;

public class PlayerInput {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean up;
    public boolean down;

    public PlayerInput() {
    }

    public void tick() {
        forward = Gdx.input.isKeyPressed(Input.Keys.W);
        backward = Gdx.input.isKeyPressed(Input.Keys.S);
        strafeLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        strafeRight = Gdx.input.isKeyPressed(Input.Keys.D);
        up = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        down = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
    }
}
