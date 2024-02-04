package com.ultreon.craft.text;

import com.ultreon.craft.util.ElementID;

import java.util.HashMap;
import java.util.Map;

public class IconMap {
    private static final ElementID CHAT_ICONS = new ElementID("chat_icons");
    private static final Map<String, FontTexture> MAPPING = new HashMap<>();

    public static FontTexture get(String name) {
        return IconMap.MAPPING.get(name);
    }

    public static void set(String name, FontTexture texture) {
        IconMap.MAPPING.put(name, texture);
    }

    public static void register() {
        IconMap.set("icon_success", new FontTexture(0x0021, IconMap.CHAT_ICONS));
        IconMap.set("icon_info", new FontTexture(0x0022, IconMap.CHAT_ICONS));
        IconMap.set("icon_warning", new FontTexture(0x0023, IconMap.CHAT_ICONS));
        IconMap.set("icon_error", new FontTexture(0x0024, IconMap.CHAT_ICONS));
        IconMap.set("icon_denied", new FontTexture(0x0025, IconMap.CHAT_ICONS));
        IconMap.set("icon_fatal", new FontTexture(0x0026, IconMap.CHAT_ICONS));
        IconMap.set("icon_debug", new FontTexture(0x0027, IconMap.CHAT_ICONS));
        IconMap.set("tag_owner", new FontTexture(0x0120, IconMap.CHAT_ICONS));
        IconMap.set("tag_admin", new FontTexture(0x0122, IconMap.CHAT_ICONS));
        IconMap.set("tag_server", new FontTexture(0x0220, IconMap.CHAT_ICONS));
        IconMap.set("tag_console", new FontTexture(0x0221, IconMap.CHAT_ICONS));
        IconMap.set("tag_death", new FontTexture(0x0222, IconMap.CHAT_ICONS));
        IconMap.set("tag_broadcast", new FontTexture(0x0223, IconMap.CHAT_ICONS));
        IconMap.set("tag_join", new FontTexture(0x0224, IconMap.CHAT_ICONS));
        IconMap.set("tag_leave", new FontTexture(0x0225, IconMap.CHAT_ICONS));
    }
}