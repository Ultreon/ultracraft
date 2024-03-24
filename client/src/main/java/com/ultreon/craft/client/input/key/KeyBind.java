package com.ultreon.craft.client.input.key;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.function.Predicate;

public class KeyBind {
    private static final int SHIFT_MOD = 0x1;
    private static final int CTRL_MOD = 0x2;
    private static final int ALT_MOD = 0x4;
    private final String name;
    private int modifiers;
    private Type type;
    private int keyCode;

    public KeyBind(String name, int keyCode, Type type) {
        this(name, keyCode, 0);
        this.type = type;
    }

    public KeyBind(String name, int keyCode, int modifiers) {
        this.name = name;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.type = Type.KEY;
    }

    public boolean isPressed() {
        return type.pressed.test(this);
    }

    public boolean isReleased() {
        return !type.pressed.test(this);
    }

    public boolean isJustPressed() {
        if ((modifiers & SHIFT_MOD) != 0 && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
            return false;
        if ((modifiers & CTRL_MOD) != 0 && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
            return false;
        if ((modifiers & ALT_MOD) != 0 && !Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
            return false;
        return type.justPressed.test(this);
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getName() {
        return this.name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public enum Type {
        KEY(key -> Gdx.input.isKeyJustPressed(key.keyCode), key -> Gdx.input.isKeyPressed(key.keyCode)),
        MOUSE(key -> Gdx.input.isButtonJustPressed(key.keyCode), key -> Gdx.input.isButtonPressed(key.keyCode));

        private final Predicate<KeyBind> justPressed;
        private final Predicate<KeyBind> pressed;

        Type(Predicate<KeyBind> justPressed, Predicate<KeyBind> pressed) {
            this.justPressed = justPressed;
            this.pressed = pressed;
        }

        public boolean isJustPressed(KeyBind key) {
            return this.justPressed.test(key);
        }

        public boolean isPressed(KeyBind key) {
            return this.pressed.test(key);
        }
    }
}
