package com.ultreon.craft.init;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

@SuppressWarnings("GDXJavaStaticResource")
public class Fonts {
    public static final BitmapFont DEFAULT = register("default", UltreonCraft.get().font);
    public static final BitmapFont MONOSPACED = register("monospaced", new BitmapFont());

    private static BitmapFont register(String name, BitmapFont font) {
        Registries.FONTS.register(new Identifier(UltreonCraft.NAMESPACE, name), font);
        return font;
    }
}
