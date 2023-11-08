package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.WorldStorage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.IOException;
import java.nio.file.Paths;

public class WorldCreationScreen extends Screen {
    @MonotonicNonNull
    private TextEntry<?> worldNameEntry;
    @MonotonicNonNull
    private Button<?> createButton;
    private Button<?> cancelButton;
    private String worldName;

    public WorldCreationScreen() {
        super(TextObject.translation("ultracraft.screen.world_creation.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.label(Alignment.CENTER, () -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 40))
                .text(this.getTitle());
        this.worldNameEntry = builder.textEntry(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 - 20), this::updateWorldName)
                .width(200)
                .hint(TextObject.translation("ultracraft.screen.world_creation.name"));

        this.createButton = builder.button(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 5), this::createWorld)
                .width(95)
                .translation("ultracraft.screen.world_creation.create");
        this.cancelButton = builder.button(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 5), this::onBack)
                .width(95)
                .translation("ultracraft.ui.cancel");
    }

    private void onBack(Button<?> caller) {
        this.back();
    }

    private void createWorld(Button<?> caller) {
        WorldStorage storage = new WorldStorage(Paths.get("worlds", this.worldName));
        try {
            storage.delete();
            storage.createWorld();
            this.client.startWorld(storage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateWorldName(TextEntry<?> caller) {
        this.worldName = caller.getRawText();
    }

    public TextEntry<?> getWorldNameEntry() {
        return this.worldNameEntry;
    }

    public Button<?> getCreateButton() {
        return this.createButton;
    }

    public String getWorldName() {
        return this.worldName;
    }
}
