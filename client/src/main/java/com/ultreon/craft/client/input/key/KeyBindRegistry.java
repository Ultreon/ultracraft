package com.ultreon.craft.client.input.key;

import com.badlogic.gdx.utils.Array;

public class KeyBindRegistry {
    private KeyBindRegistry() {

    }

    static final Array<KeyBind> KEY_BINDS = new Array<>();

    public static KeyBind register(KeyBind keyBind) {
        KeyBindRegistry.KEY_BINDS.add(keyBind);
        return keyBind;
    }

    public static void unregister(KeyBind keyBind) {
        KEY_BINDS.removeValue(keyBind, true);
    }

    public static KeyBind[] getKeyBinds() {
        return KEY_BINDS.items;
    }
}
