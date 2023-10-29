package com.ultreon.craft.client.gui.screen;

import com.ultreon.libs.commons.v0.Identifier;

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
