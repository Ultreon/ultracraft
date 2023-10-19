package com.ultreon.craft.render.texture.atlas;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.UV;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.HashMap;
import java.util.Map;

import static com.ultreon.craft.UltreonCraft.id;
import static com.ultreon.craft.UltreonCraft.isOnMainThread;

public class TextureStitcher {
    private final Map<Identifier, Texture> textures = new HashMap<>();
    private FrameBuffer fbo;

    public void add(Identifier id, Texture tex) {
        this.textures.put(id, tex);
    }

    public TextureAtlas stitch() {
        if (!isOnMainThread()) {
            return UltreonCraft.get().getAndWait(id("texture_stitcher/stitch"), this::stitch);
        }

        UltreonCraft ultracraft = UltreonCraft.get();

        // Determine the dimensions of the final texture atlas
        int width = 1024; // calculate the width of the atlas
        int height = 1024; // calculate the height of the atlas
        {
            int x = 0;
            int y = 0;
            int texHeight = 0;
            for (Texture tex : this.textures.values()) {
                texHeight = Math.max(tex.getHeight(), texHeight);
                x += tex.getWidth();
                if (x + tex.getWidth() > width) {
                    x = 0;
                    y += texHeight;
                    texHeight = 0;
                    height = y;
                }
            }
        }

        // Create a temporary FrameBuffer to hold the packed textures
        int finalHeight = height;
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, finalHeight, false);

        // Create a SpriteBatch to draw the packed textures to the FrameBuffer
        SpriteBatch spriteBatch = new SpriteBatch();
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

        // Draw each texture to the appropriate location on the FrameBuffer
        this.fbo.begin();
        spriteBatch.begin();
        int x = 0;
        int y = 0;
        int texHeight = 0;

        ImmutableMap.Builder<Identifier, UV> uvMap = new ImmutableMap.Builder<>();

        for (Map.Entry<Identifier, Texture> e : this.textures.entrySet()) {
            Texture texture = e.getValue();
            Identifier id = e.getKey();
            spriteBatch.draw(texture, x, y);

            UV uv = new UV(x, y + texture.getHeight(), texture.getWidth(), -texture.getHeight(), width, height);
            uvMap.put(id, uv);

            texHeight = Math.max(texture.getHeight(), texHeight);
            x += texture.getWidth();
            if (x + texture.getWidth() > width) {
                x = 0;
                y += texHeight;
                texHeight = 0;
            }
        }
        spriteBatch.end();
        this.fbo.end();

        // Create a new Texture from the packed FrameBuffer
        Texture textureAtlas = this.fbo.getColorBufferTexture();
        textureAtlas.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        textureAtlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Clean up resources
        spriteBatch.dispose();

        return new TextureAtlas(this, textureAtlas, uvMap.build());
    }

    public void dispose() {
        this.fbo.dispose();
    }
}
