package com.ultreon.craft.text;

import com.ultreon.libs.commons.v0.Identifier;

import java.util.HashMap;
import java.util.Map;

public class EmoteMap {
    private static final Identifier font = new Identifier("ultracraft:emotes");
    private static final Map<String, FontTexture> mapping = new HashMap<>();

    public static FontTexture get(String name) {
        return EmoteMap.mapping.get(name);
    }

    public static void set(String name, FontTexture texture) {
        EmoteMap.mapping.put(name, texture);
    }

    public static void register() {
        EmoteMap.set("smile", new FontTexture(0x0021, EmoteMap.font));
        EmoteMap.set("laugh", new FontTexture(0x0022, EmoteMap.font));
        EmoteMap.set("concern", new FontTexture(0x0024, EmoteMap.font));
    }
}