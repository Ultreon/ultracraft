package com.ultreon.craft.client.input;

import com.badlogic.gdx.Input;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.client.input.key.KeyBind;
import com.ultreon.craft.client.input.key.KeyBindRegistry;

public class KeyBinds {
    private KeyBinds() {

    }

    public static final KeyBind pauseKey = KeyBindRegistry.register(new KeyBind("pause", GamePlatform.get().isMobile() ? Input.Buttons.BACK : Input.Keys.ESCAPE, KeyBind.Type.KEY));
    public static final KeyBind imGuiKey = KeyBindRegistry.register(new KeyBind("imGui", Input.Keys.F9, KeyBind.Type.KEY));
    public static final KeyBind imGuiFocusKey = KeyBindRegistry.register(new KeyBind("imGuiFocus", Input.Keys.F10, KeyBind.Type.KEY));
    public static final KeyBind inspectKey = KeyBindRegistry.register(new KeyBind("inspect", Input.Keys.F4, KeyBind.Type.KEY));
    public static final KeyBind inventoryKey = KeyBindRegistry.register(new KeyBind("inventory", Input.Keys.E, KeyBind.Type.KEY));
    public static final KeyBind chatKey = KeyBindRegistry.register(new KeyBind("chat", Input.Keys.PERIOD, KeyBind.Type.KEY));
    public static final KeyBind commandKey = KeyBindRegistry.register(new KeyBind("command", Input.Keys.SLASH, KeyBind.Type.KEY));
    public static final KeyBind thirdPersonKey = KeyBindRegistry.register(new KeyBind("thirdPerson", Input.Keys.F5, KeyBind.Type.KEY));
    public static final KeyBind hideHudKey = KeyBindRegistry.register(new KeyBind("hideHud", Input.Keys.F1, KeyBind.Type.KEY));
    public static final KeyBind screenshotKey = KeyBindRegistry.register(new KeyBind("screenshot", Input.Keys.F2, KeyBind.Type.KEY));
    public static final KeyBind fullScreenKey = KeyBindRegistry.register(new KeyBind("fullScreen", Input.Keys.F11, KeyBind.Type.KEY));
    public static final KeyBind debugKey = KeyBindRegistry.register(new KeyBind("debug", Input.Keys.F3, KeyBind.Type.KEY));
    public static final KeyBind walkForwardsKey = KeyBindRegistry.register(new KeyBind("walkForward", Input.Keys.W, KeyBind.Type.KEY));
    public static final KeyBind walkBackwardsKey = KeyBindRegistry.register(new KeyBind("walkBackwards", Input.Keys.S, KeyBind.Type.KEY));
    public static final KeyBind walkLeftKey = KeyBindRegistry.register(new KeyBind("walkLeft", Input.Keys.A, KeyBind.Type.KEY));
    public static final KeyBind walkRightKey = KeyBindRegistry.register(new KeyBind("walkRight", Input.Keys.D, KeyBind.Type.KEY));
    public static final KeyBind jumpKey = KeyBindRegistry.register(new KeyBind("jump", Input.Keys.SPACE, KeyBind.Type.KEY));
    public static final KeyBind crouchKey = KeyBindRegistry.register(new KeyBind("crouch", Input.Keys.SHIFT_LEFT, KeyBind.Type.KEY));
    public static final KeyBind runningKey = KeyBindRegistry.register(new KeyBind("running", Input.Keys.CONTROL_LEFT, KeyBind.Type.KEY));
}
