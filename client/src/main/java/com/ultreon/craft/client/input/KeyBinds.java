package com.ultreon.craft.client.input;

import com.badlogic.gdx.Input;

public class KeyBinds {
    private KeyBinds() {

    }


    public static final KeyBind pauseKey = KeyBindRegistry.register(new KeyBind("pause", Input.Keys.ESCAPE));
    public static final KeyBind imGuiKey = KeyBindRegistry.register(new KeyBind("imGui", Input.Keys.F9));
    public static final KeyBind imGuiFocusKey = KeyBindRegistry.register(new KeyBind("imGuiFocus", Input.Keys.F10));
    public static final KeyBind inspectKey = KeyBindRegistry.register(new KeyBind("inspect", Input.Keys.F4));
    public static final KeyBind inventoryKey = KeyBindRegistry.register(new KeyBind("inventory", Input.Keys.E));
    public static final KeyBind thirdPersonKey = KeyBindRegistry.register(new KeyBind("thirdPerson", Input.Keys.F5));
    public static final KeyBind screenshotKey = KeyBindRegistry.register(new KeyBind("screenshot", Input.Keys.F2));
    public static final KeyBind fullScreenKey = KeyBindRegistry.register(new KeyBind("fullScreen", Input.Keys.F11));
    public static final KeyBind debugKey = KeyBindRegistry.register(new KeyBind("debug", Input.Keys.F3));
    public static final KeyBind walkForwardsKey = KeyBindRegistry.register(new KeyBind("walkForward", Input.Keys.W));
    public static final KeyBind walkBackwardsKey = KeyBindRegistry.register(new KeyBind("walkBackwards", Input.Keys.S));
    public static final KeyBind walkLeftKey = KeyBindRegistry.register(new KeyBind("walkLeft", Input.Keys.A));
    public static final KeyBind walkRightKey = KeyBindRegistry.register(new KeyBind("walkRight", Input.Keys.D));
    public static final KeyBind jumpKey = KeyBindRegistry.register(new KeyBind("jump", Input.Keys.SPACE));
    public static final KeyBind crouchKey = KeyBindRegistry.register(new KeyBind("crouch", Input.Keys.SHIFT_LEFT));
    public static final KeyBind runningKey = KeyBindRegistry.register(new KeyBind("running", Input.Keys.CONTROL_LEFT));
}
