package com.ultreon.craft.client.init;

import com.ultreon.craft.client.ClientRegistries;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.libs.commons.v0.Identifier;

@SuppressWarnings("GDXJavaStaticResource")
public class Fonts {
    public static final Font DEFAULT = Fonts.register("default", UltracraftClient.get().font);

    private static Font register(String name, Font font) {
        ClientRegistries.FONT.register(new Identifier(UltracraftClient.NAMESPACE, name), font);
        return font;
    }

    public static void nopInit() {
        // Empty for class initialization
    }
}
