package com.ultreon.craft.input;

import com.badlogic.gdx.Input;

public class PlayerInput {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean up;
    public boolean down;

    public void tick() {
        forward = InputManager.isKeyDown(Input.Keys.W);
        backward = InputManager.isKeyDown(Input.Keys.S);
        strafeLeft = InputManager.isKeyDown(Input.Keys.A);
        strafeRight = InputManager.isKeyDown(Input.Keys.D);
        up = InputManager.isKeyDown(Input.Keys.SPACE);
        down = InputManager.isKeyDown(Input.Keys.SHIFT_LEFT);
    }
}
