package com.ultreon.craft.client.texture;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.resources.StaticResource;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.Identifier;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {

    private final Map<Identifier, Texture> textures = new HashMap<>();

    private final ResourceManager resourceManager;

    @Deprecated
    public static final StaticResource DEFAULT_TEX_RESOURCE = new StaticResource(TextureManager::createDefaultTex);

    public static final Texture DEFAULT_TEX = new Texture(TextureManager.createMissingNo());
    public static final TextureRegion DEFAULT_TEX_REG = new TextureRegion(TextureManager.DEFAULT_TEX, 0.0F, 0.0F, 1.0F, 1.0F);
    @Deprecated
    public static final TextureRegion DEFAULT_TEXTURE_REG = TextureManager.DEFAULT_TEX_REG;

    static {
        TextureManager.DEFAULT_TEX.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        TextureManager.DEFAULT_TEX.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private boolean frozen = false;

    public TextureManager(ResourceManager resourceManager) {
        Preconditions.checkNotNull(resourceManager, "resourceManager");

        this.resourceManager = resourceManager;
    }

    private static Pixmap createMissingNo() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGB888);
        pixmap.setColor(Color.rgb(0x000000).toGdx());
        pixmap.fillRectangle(0, 0, 16, 16);
        pixmap.setColor(Color.rgb(0xff00ff).toGdx());
        pixmap.fillRectangle(0, 0, 8, 8);
        pixmap.fillRectangle(8, 8, 16, 16);
        return pixmap;
    }

    private static InputStream createDefaultTex() throws IOException {
        var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.getGraphics();
        graphics.setColor(Color.rgb(0xffbb00).toAwt());
        graphics.fillRect(0, 0, 16, 16);
        graphics.setColor(Color.rgb(0x333333).toAwt());
        graphics.fillRect(0, 8, 8, 8);
        graphics.fillRect(8, 0, 8, 8);
        var out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        graphics.dispose();
        out.flush();
        var byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
        out.close();
        return byteArrayInputStream;
    }

    public Texture getTexture(Identifier id, Texture fallback) {
        Preconditions.checkNotNull(id, "id");

        if (!UltracraftClient.isOnMainThread()) {
            return UltracraftClient.invokeAndWait(() -> this.getTexture(id, fallback));
        }

        if (!this.textures.containsKey(id)) {
            return this.registerTextureFB(id, fallback);
        }

        Texture texture = this.textures.get(id);
        if (texture == null) return fallback;

        return texture;
    }

    @NotNull
    public Texture getTexture(Identifier id) {
        return this.getTexture(id, TextureManager.DEFAULT_TEX);
    }

    public boolean isTextureLoaded(Identifier id) {
        if (this.frozen) return false;

        Preconditions.checkNotNull(id, "id");

        return this.textures.containsKey(id);
    }

    @NotNull
    @NewInstance
    @CanIgnoreReturnValue
    public Texture registerTexture(Identifier id) {
        if (this.frozen) return TextureManager.DEFAULT_TEX;

        Preconditions.checkNotNull(id, "id");
        Texture oldTexture = this.textures.get(id);
        if (oldTexture != null) return oldTexture;

        FileHandle handle = UltracraftClient.resource(id);
        if (!handle.exists()) {
            UltracraftClient.LOGGER.warn("Texture not found: " + id);
            this.textures.put(id, TextureManager.DEFAULT_TEX);
            return TextureManager.DEFAULT_TEX;
        }

        Pixmap pixmap = new Pixmap(handle);

        Texture texture = new Texture(pixmap);
        if (texture.getTextureData() == null) {
            UltracraftClient.LOGGER.warn("Couldn't read texture data: " + id);
            this.textures.put(id, TextureManager.DEFAULT_TEX);
            return TextureManager.DEFAULT_TEX;
        }

        this.textures.put(id, texture);
        return texture;
    }

    @NewInstance
    @CanIgnoreReturnValue
    public Texture registerTextureFB(Identifier id, Texture fallback) {
        if (this.frozen) return fallback;

        Preconditions.checkNotNull(id, "id");
        Texture oldTexture = this.textures.get(id);
        if (oldTexture != null) return oldTexture;

        FileHandle handle = UltracraftClient.resource(id);
        if (!handle.exists()) {
            if (fallback != null) UltracraftClient.LOGGER.warn("Texture not found: {}", id);
            this.textures.put(id, fallback);
            return fallback;
        }

        Pixmap pixmap = new Pixmap(handle);

        Texture texture = new Texture(pixmap);
        if (texture.getTextureData() == null) {
            if (fallback != null) UltracraftClient.LOGGER.warn("Couldn't read texture data: {}", id);
            this.textures.put(id, fallback);
            return fallback;
        }

        this.textures.put(id, texture);
        return texture;
    }

    @CanIgnoreReturnValue
    public Texture registerTexture(@NotNull Identifier id, @NotNull Texture texture) {
        if (this.frozen) return TextureManager.DEFAULT_TEX;

        Preconditions.checkNotNull(id, "id");
        Preconditions.checkNotNull(texture, "texture");

        if (this.textures.containsKey(id)) throw new IllegalArgumentException("A texture is already registered with id: " + id);
        if (texture.getTextureData() == null) return TextureManager.DEFAULT_TEX;

        this.textures.put(id, texture);
        return texture;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public void dispose() {
        this.frozen = true;
        for (Texture texture : this.textures.values()) {
            texture.dispose();
        }
    }
}
