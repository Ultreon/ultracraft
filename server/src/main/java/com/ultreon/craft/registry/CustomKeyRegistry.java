package com.ultreon.craft.registry;

import com.ultreon.craft.text.TextKey;
import com.ultreon.craft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CustomKeyRegistry {

    private static final Map<Identifier, TextKey> REGISTRY = new HashMap<>();

    public static void register(Identifier id, TextKey key) {
        if (REGISTRY.containsKey(id))
            throw new IllegalArgumentException("Key already registered: " + id);

        REGISTRY.put(id, key);
    }

    public static TextKey get(Identifier id) {
        return REGISTRY.get(id);
    }
}
