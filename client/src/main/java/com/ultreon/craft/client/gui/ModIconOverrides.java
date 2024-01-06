package com.ultreon.craft.client.gui;

import com.ultreon.craft.util.ElementID;

import java.util.HashMap;
import java.util.Map;

public class ModIconOverrides {
    private static final Map<String, ElementID> OVERRIDES = new HashMap<>();

    public static ElementID get(String modId) {
        return ModIconOverrides.OVERRIDES.get(modId);
    }

    public static void set(String modId, ElementID iconId) {
        ModIconOverrides.OVERRIDES.put(modId, iconId);
    }
}
