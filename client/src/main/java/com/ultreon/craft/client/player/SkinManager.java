package com.ultreon.craft.client.player;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.api.events.ClientReloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SkinManager {
    private CompletableFuture<Texture> future = loadAsync();

    @NotNull
    private static CompletableFuture<Texture> loadAsync() {
        return CompletableFuture.supplyAsync(() -> {
            FileHandle data = UltracraftClient.data("skin.png");
            if (!data.exists()) return null;
            Pixmap pixmap = new Pixmap(data);

            return UltracraftClient.invokeAndWait(() -> {
                Texture texture = new Texture(pixmap, false);
                ClientReloadEvent.SKIN_LOADED.factory().onSkinLoaded(texture, pixmap);

                pixmap.dispose();
                return texture;
            });
        });
    }

    public Texture getLocalSkin() {
        return getOrNull();
    }

    private Texture getOrNull() {
        return future.getNow(null);
    }

    public void reloadResources() {

    }

    public void reload() {
        if (getOrNull() == null) return;
        getOrNull().dispose();
        future = loadAsync();

        ClientReloadEvent.SKIN_RELOAD.factory().onSkinReload();
    }
}
