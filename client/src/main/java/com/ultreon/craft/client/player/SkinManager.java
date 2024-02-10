package com.ultreon.craft.client.player;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;

import java.util.concurrent.CompletableFuture;

public class SkinManager {
    public static final CompletableFuture<Texture> future = CompletableFuture.supplyAsync(() -> {
        FileHandle data = UltracraftClient.data("skin.png");
        if (!data.exists()) return null;
        Pixmap pixmap = new Pixmap(data);

        return UltracraftClient.invokeAndWait(() -> new Texture(pixmap, false));
    });

    public Texture getLocalSkin() {
        return getOrNull();
    }

    private Texture getOrNull() {
        return future.getNow(null);
    }
}
