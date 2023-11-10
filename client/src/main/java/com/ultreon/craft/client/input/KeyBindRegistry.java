package com.ultreon.craft.client.input;

import com.badlogic.gdx.utils.Array;

public class KeyBindRegistry {
    private KeyBindRegistry() {

    }

    static final Array<KeyBind> KEY_BINDS = new Array<>();

    public static KeyBind register(KeyBind keyBind) {
        KeyBindRegistry.KEY_BINDS.add(keyBind);
        return keyBind;
    }
}
