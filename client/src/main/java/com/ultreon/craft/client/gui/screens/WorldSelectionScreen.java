package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.world.WorldStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldSelectionScreen extends Screen {
    public static final Path WORLDS_DIR = Paths.get("worlds");
    private SelectionList<WorldStorage> worldList;
    private WorldStorage selected;
    private TextButton createButton;
    private TextButton playButton;
    private TextButton deleteWorld;

    public WorldSelectionScreen() {
        super(TextObject.translation("ultracraft.screen.world_selection.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, 10))
                .scale(2));

        this.worldList = builder.add(new SelectionList<WorldStorage>()
                .itemHeight(20)
                .bounds(() -> new Bounds(170, 40, this.getWidth() - 170, this.getHeight()))
                .entries(this.locateWorlds())
                .itemRenderer(this::renderItem)
                .selectable(true)
                .callback(this::selectWorld));

        this.createButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.world_selection.create"), 150)
                .position(() -> new Position(10, this.getHeight() / 2 - 20))
                .callback(this::createWorld));

        this.playButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.world_selection.play"), 150)
                .position(() -> new Position(10, this.getHeight() / 2 + 5))
                .callback(this::playWorld));

        this.deleteWorld = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.world_selection.delete"), 150)
                .position(() -> new Position(10, this.getHeight() / 2 + 30))
                .callback(this::deleteWorld));
    }

    private void deleteWorld(TextButton caller) {
        if (this.selected == null) return;

        this.client.showScreen(new WorldDeleteConfirmScreen(this.selected));
    }

    private void playWorld(TextButton t) {
        if (this.selected == null) return;

        WorldStorage selected = this.selected;
        this.client.startWorld(selected);

    }

    private void createWorld(TextButton caller) {
        this.client.showScreen(new WorldCreationScreen());
    }

    private void selectWorld(WorldStorage storage) {
        this.selected = storage;
    }

    private void renderItem(Renderer renderer, WorldStorage storage, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        renderer.textLeft(storage.getDirectory().getFileName().toString(), this.worldList.getX(), y + 5, selected ? Color.WHITE : Color.GRAY);
    }

    public List<WorldStorage> locateWorlds() {
        var worlds = new ArrayList<WorldStorage>();
        try (Stream<Path> worldPaths = Files.list(WorldSelectionScreen.WORLDS_DIR)) {
            worlds = worldPaths.map(WorldStorage::new).sorted(Comparator.comparing(o -> o.getDirectory().getFileName().toString())).collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ignored) {
            // ignored
        }

        return worlds;
    }

    public SelectionList<WorldStorage> getWorldList() {
        return this.worldList;
    }

    public WorldStorage getSelected() {
        return this.selected;
    }

    public TextButton getCreateButton() {
        return this.createButton;
    }

    public TextButton getDeleteWorld() {
        return this.deleteWorld;
    }

    public TextButton getPlayButton() {
        return this.playButton;
    }
}
