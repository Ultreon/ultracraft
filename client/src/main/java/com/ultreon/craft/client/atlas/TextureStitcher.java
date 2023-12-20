package com.ultreon.craft.client.atlas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.TextureOffset;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.ultreon.craft.client.UltracraftClient.isOnMainThread;

public class TextureStitcher {
    private final Map<Identifier, Texture> textures = new HashMap<>();
    private final Map<Identifier, Texture> emissiveTextures = new HashMap<>();
    private FrameBuffer fbo;
    private Identifier atlasId;

    public TextureStitcher(Identifier atlasId) {
        this.atlasId = atlasId;
    }

    public void add(Identifier id, Texture diffuse) {
        this.textures.put(id, diffuse);
    }

    public void add(Identifier id, Texture diffuse, Texture emissive) {
        this.textures.put(id, diffuse);
        this.emissiveTextures.put(id, emissive);
    }

    public TextureAtlas stitch() {
        if (!isOnMainThread()) {
            return UltracraftClient.invokeAndWait(this::stitch);
        }

        // Determine the dimensions of the final texture atlas
        int width = 2048; // calculate the width of the atlas
        int height = 2048;

        Result diffuseResult = this.generateAtlas(width, height, Type.DIFFUSE, this.textures, Collections.emptyMap());
        Map<Identifier, TextureOffset> map = diffuseResult.uvMap().build();

        Result emissiveResult = this.generateAtlas(width, height, Type.EMISSIVE, this.emissiveTextures, map);

        return new TextureAtlas(this, diffuseResult.textureAtlas(), emissiveResult.textureAtlas(), map);
    }

    @NotNull
    private Result generateAtlas(int width, int height, Type type, Map<Identifier, Texture> texMap, Map<Identifier, TextureOffset> oldUVMap) {
        // Create a temporary DepthFrameBuffer to hold the packed textures
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);

        // Create a SpriteBatch to draw the packed textures to the DepthFrameBuffer
        SpriteBatch spriteBatch = new SpriteBatch();
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

        // Draw each texture to the appropriate location on the DepthFrameBuffer
        this.fbo.begin();
        spriteBatch.begin();
        int x = 0;
        int y = 0;
        int texHeight = 0;

        ImmutableMap.Builder<Identifier, TextureOffset> uvMap = new ImmutableMap.Builder<>();

        for (var e : texMap.entrySet()) {
            Texture texture = e.getValue();
            Identifier id = e.getKey();
            TextureRegion region = new TextureRegion(texture);
            TextureOffset textureOffset = oldUVMap.get(id);
            if (textureOffset == null && type == Type.DIFFUSE) {
                textureOffset = new TextureOffset(x, y + texture.getHeight(), texture.getWidth(), -texture.getHeight(), width, height);
            } else if (textureOffset == null) {
                throw new IllegalStateException("No diffuse texture for: " + id);
            }

            region.flip(false, true);
            spriteBatch.draw(region, textureOffset.u(), textureOffset.v() - texture.getHeight());

            uvMap.put(id, textureOffset);

            texHeight = Math.max(texture.getHeight(), texHeight);
            x += texture.getWidth();
            if (x + texture.getWidth() > width) {
                x = 0;
                y += texHeight;
                texHeight = 0;
            }
        }
        spriteBatch.end();

        if (DebugFlags.DUMP_TEXTURE_ATLAS) {
            Pixmap frameBufferPixmap = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);
            PixmapIO.writePNG(Gdx.files.local("%s.%s.atlas-png".formatted(this.atlasId.toString().replace(':', '.').replace('/', '_'), type.name().toLowerCase(Locale.ROOT))), frameBufferPixmap);
        }

        this.fbo.end();

        // Create a new Texture from the packed DepthFrameBuffer
        Texture textureAtlas = this.fbo.getColorBufferTexture();
        textureAtlas.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        textureAtlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Clean up resources
        spriteBatch.dispose();

        return new Result(spriteBatch, uvMap, textureAtlas);
    }

    private record Result(SpriteBatch spriteBatch, ImmutableMap.Builder<Identifier, TextureOffset> uvMap, Texture textureAtlas) {
    }

    private int calcHeight(int width) {
        int height = 1024; // calculate the height of the atlas
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
        return height;
    }

    public void dispose() {
        this.textures.values().forEach(Texture::dispose);
        this.fbo.dispose();
    }

    public enum Type {
        DIFFUSE, EMISSIVE
    }
}
