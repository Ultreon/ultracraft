package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Notification;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.icon.GenericIcon;
import com.ultreon.craft.client.gui.icon.MessageIcon;
import com.ultreon.craft.client.gui.widget.IconButton;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.client.text.WordGenerator;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.WorldStorage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.IOException;
import java.nio.file.Paths;

public class WorldCreationScreen extends Screen {
    private static final WordGenerator WORD_GEN = new WordGenerator(new WordGenerator.Config().minSize(4).maxSize(6).named());
    @MonotonicNonNull
    private TextEntry worldNameEntry;
    @MonotonicNonNull
    private IconButton reloadButton;
    @MonotonicNonNull
    private TextButton createButton;
    private String worldName = "";

    public WorldCreationScreen() {
        super(TextObject.translation("ultracraft.screen.world_creation.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        var titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 60))
                .scale(2));

        titleLabel.text().set(this.getTitle());
        titleLabel.scale().set(2);

        this.worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();
        this.worldNameEntry = builder.add(TextEntry.of(this.worldName).position(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 - 20))
                .callback(this::updateWorldName)
                .hint(TextObject.translation("ultracraft.screen.world_creation.name")));

        this.reloadButton = builder.add(IconButton.of(GenericIcon.RELOAD).position(() -> new Position(this.getWidth() / 2 + 105, this.getHeight() / 2 - 24))
                .callback(this::regenerateName));

        this.createButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.world_creation.create"), 95)
                .position(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 5))
                .callback(this::createWorld));

        builder.add(TextButton.of(UITranslations.CANCEL, 95)
                .position(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 5))
                .callback(this::onBack));
    }

    private void regenerateName(IconButton iconButton) {
        this.worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();
        this.worldNameEntry.value(this.worldName);
    }

    private void onBack(TextButton caller) {
        this.back();
    }

    private void createWorld(TextButton caller) {
        WorldStorage storage = new WorldStorage(Paths.get("worlds", this.worldName));
        try {
            storage.delete();
            storage.createWorld();
            this.client.startWorld(storage);
        } catch (IOException e) {
            this.client.notifications.add(Notification.builder(TextObject.literal("Failed to create world"), TextObject.literal(e.getLocalizedMessage())).icon(MessageIcon.ERROR).build());
        }
    }

    private void updateWorldName(TextEntry caller) {
        this.worldName = caller.getValue();
    }

    public TextEntry getWorldNameEntry() {
        return this.worldNameEntry;
    }

    public TextButton getCreateButton() {
        return this.createButton;
    }

    public IconButton getReloadButton() {
        return this.reloadButton;
    }

    public String getWorldName() {
        return this.worldName;
    }
}
