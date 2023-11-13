package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Notification;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.icon.MessageIcon;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.WorldStorage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.IOException;
import java.nio.file.Paths;

public class WorldCreationScreen extends Screen {
    @MonotonicNonNull
    private TextEntry worldNameEntry;
    @MonotonicNonNull
    private Button createButton;
    private String worldName;

    public WorldCreationScreen() {
        super(TextObject.translation("ultracraft.screen.world_creation.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        var titleLabel = builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 45));
        titleLabel.text().set(this.getTitle());
        titleLabel.scale().set(2);

        this.worldNameEntry = builder.addWithPos(new TextEntry(200), () -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 - 20));
        this.worldNameEntry.callback().set(this::updateWorldName);
        this.worldNameEntry.hint().translate("ultracraft.screen.world_creation.name");

        this.createButton = builder.addWithPos(new Button(95), () -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 5));
        this.createButton.callback().set(this::createWorld);
        this.createButton.text().translate("ultracraft.screen.world_creation.create");

        var cancelButton = builder.addWithPos(new Button(95), () -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 5));
        cancelButton.callback().set(this::onBack);
        cancelButton.text().translate("ultracraft.ui.cancel");
    }

    private void onBack(Button caller) {
        this.back();
    }

    private void createWorld(Button caller) {
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

    public Button getCreateButton() {
        return this.createButton;
    }

    public String getWorldName() {
        return this.worldName;
    }
}
