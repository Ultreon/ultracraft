package com.ultreon.craft.client.gui;

import com.ultreon.craft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ModIconOverrides {
    private static final Map<String, Identifier> OVERRIDES = new HashMap<>();

    public static Identifier get(String modId) {
        return ModIconOverrides.OVERRIDES.get(modId);
    }

    public static void set(String modId, Identifier iconId) {
        ModIconOverrides.OVERRIDES.put(modId, iconId);
    }
}
