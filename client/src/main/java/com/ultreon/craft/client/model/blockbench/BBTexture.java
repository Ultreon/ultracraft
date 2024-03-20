package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ByteArrayFileHandle;
import com.ultreon.craft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public record BBTexture(String path, String name, String folder, String namespace, String id, int width, int height,
                        int uvWidth, int uvHeight, boolean particle, boolean layersEnabled, String syncToProject,
                        String renderMode, String renderSides, int frameTime, String frameOrderType, String frameOrder,
                        boolean frameInterpolate, boolean visible, boolean internal, boolean saved, UUID uuid,
                        String relativePath, java.net.URL data) {
    public Texture loadOrGetTexture() throws IOException {
        try (InputStream inputStream = data.openStream()) {
            Texture texture = new Texture(new ByteArrayFileHandle(".png", inputStream.readAllBytes()));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            return texture;
        }
    }
}
