package com.ultreon.craft.client.rpc;

import com.ultreon.craft.client.UltracraftClient;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

public enum GameActivity {
    MAIN_MENU("Main menu"),
    SINGLEPLAYER("Playing singleplayer"),
    MULTIPLAYER("Playing multiplayer", () -> {
        UltracraftClient client = UltracraftClient.get();
        if (client.serverData == null || client.config.get().privacy.hideActiveServer) {
            return null;
        }
        return "On " + client.serverData.name();
    });

    private final String displayName;
    private final Callable<@Nullable String> description;

    GameActivity(String displayName) {
        this(displayName, () -> null);
    }

    GameActivity(String displayName, Callable<@Nullable String> description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public @Nullable String getDescription() throws Exception {
        return this.description.call();
    }
}
