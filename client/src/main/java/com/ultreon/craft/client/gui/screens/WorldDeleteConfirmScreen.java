package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Notification;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.icon.MessageIcon;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class WorldDeleteConfirmScreen extends Screen {
    @NotNull
    private final WorldStorage storage;

    public WorldDeleteConfirmScreen(@NotNull WorldStorage storage) {
        super(TextObject.translation("ultracraft.screen.world_delete_confirm.title").setColor(Color.rgb(0xff4040)));
        this.storage = storage;
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title).alignment(Alignment.CENTER).textColor(Color.RED).position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .scale(2));

        builder.add(Label.of(TextObject.translation("ultracraft.screen.world_delete_confirm.message1", this.storage.getDirectory().getFileName().toString()))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2)));

        builder.add(Label.of("ultracraft.screen.world_delete_confirm.message2")
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 + 15)));

        builder.add(TextButton.of(TextObject.translation("ultracraft.ui.yes"), 95)
                .position(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 50))
                .callback(this::deleteWorld));

        builder.add(TextButton.of(TextObject.translation("ultracraft.ui.no"), 95)
                .position(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 50))
                .callback(this::onBack));
    }

    private void deleteWorld(TextButton caller) {
        try {
            Path name = this.storage.getDirectory().getFileName();
            this.storage.delete();
            this.client.notifications.add(Notification.builder("World Deleted", "'%s'".formatted(name)).subText("World Manager").icon(MessageIcon.DANGER).build());
        } catch (IOException e) {
            UltracraftClient.crash(e);
        }
        this.back();
    }

    private void onBack(TextButton caller) {
        this.back();
    }

    public @NotNull WorldStorage getStorage() {
        return this.storage;
    }
}
