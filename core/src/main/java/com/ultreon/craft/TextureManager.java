package com.ultreon.craft;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Preconditions;
import com.ultreon.craft.render.Color;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    @SuppressWarnings("GDXJavaStaticResource")
    public static final Texture DEFAULT_TEXTURE = new Texture(genMissingNo());
    public static final TextureRegion DEFAULT_TEXTURE_REG = new TextureRegion(DEFAULT_TEXTURE, 0.0F, 0.0F, 1.0F, 1.0F);

    private static Pixmap genMissingNo() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGB888);
        pixmap.setColor(Color.rgb(0x000000).toGdx());
        pixmap.fillRectangle(0, 0, 16, 16);
        pixmap.setColor(Color.rgb(0xff00ff).toGdx());
        pixmap.fillRectangle(0, 0, 8, 8);
        pixmap.fillRectangle(8, 8, 16, 16);
        return pixmap;
    }

    private final Map<Identifier, Texture> cache = new HashMap<>();

    public Texture getTexture(Identifier id) {
        if (!cache.containsKey(id)) {
            registerTexture(id);
        }
        return Preconditions.checkNotNull(cache.get(id), "Texture not registered");
    }

    public Texture registerTexture(Identifier id) {
        Texture tex = new Texture("assets/" + id.location() + "/" + id.path());
        cache.put(id, tex);
        return tex;
    }
}
