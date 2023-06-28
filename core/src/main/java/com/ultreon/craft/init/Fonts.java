package com.ultreon.craft.init;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.font.Font;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

@SuppressWarnings("GDXJavaStaticResource")
public class Fonts {
    public static final Font DEFAULT = register("default", UltreonCraft.get().font);

    private static Font register(String name, Font font) {
        Registries.FONTS.register(new Identifier(UltreonCraft.NAMESPACE, name), font);
        return font;
    }

    public static void nopInit() {

    }
}
