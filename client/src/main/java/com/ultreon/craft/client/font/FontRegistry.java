package com.ultreon.craft.client.font;

import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.registries.v0.exception.RegistryException;

import java.util.*;

public class FontRegistry {
    private static final Map<Identifier, Font> all = new HashMap<>();
    private static boolean frozen;

    public static void registerFont(Identifier identifier, Font font) {
        if (FontRegistry.frozen) throw new RegistryException("Registry frozen! ❄️");
        FontRegistry.all.put(identifier, font);
    }

    public static void freeze() {
        FontRegistry.frozen = true;
    }

    public static Collection<Font> getAll() {
        return FontRegistry.all.values();
    }

    public static boolean isFrozen() {
        return FontRegistry.frozen;
    }
}
