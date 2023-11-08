package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WorldDeleteConfirmScreen extends Screen {
    @NotNull
    private final WorldStorage storage;

    public WorldDeleteConfirmScreen(@NotNull WorldStorage storage) {
        super(TextObject.translation("ultracraft.screen.world_delete_confirm.title").setColor(Color.rgb(0xff4040)));
        this.storage = storage;
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.label(Alignment.CENTER, () -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .text(this.getTitle())
                .scale(2)
                .textColor(Color.RED);
        builder.label(Alignment.CENTER, () -> new Position(this.getWidth() / 2, this.getHeight() / 2))
                .translation("ultracraft.screen.world_delete_confirm.message1", this.storage.getDirectory().getFileName().toString());
        builder.label(Alignment.CENTER, () -> new Position(this.getWidth() / 2, this.getHeight() / 2 + 15))
                .translation("ultracraft.screen.world_delete_confirm.message2");
        builder.button(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 50), this::deleteWorld)
                .translation("ultracraft.ui.yes")
                .width(95);
        builder.button(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 50), this::onBack)
                .translation("ultracraft.ui.no")
                .width(95);
    }

    private void deleteWorld(Button<?> caller) {
        try {
            this.storage.delete();
        } catch (IOException e) {
            UltracraftClient.crash(e);
        }
        this.back();
    }

    private void onBack(Button<?> caller) {
        this.back();
    }

    public @NotNull WorldStorage getStorage() {
        return this.storage;
    }
}
