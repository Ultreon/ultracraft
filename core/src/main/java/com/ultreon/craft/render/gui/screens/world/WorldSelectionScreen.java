package com.ultreon.craft.render.gui.screens.world;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.craft.render.gui.widget.SelectionList;
import com.ultreon.craft.datastorage.SaveManager;
import com.ultreon.craft.text.CommonTexts;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.libs.translations.v1.Language;

import java.io.IOException;

public class WorldSelectionScreen extends Screen {
    private Button openButton;
    private Button modifyButton;
    private Button createButton;
    private Button deleteButton;
    private Button backButton;
    private SelectionList<SavedWorld> list;
    private final GlyphLayout layout = new GlyphLayout();

    public WorldSelectionScreen() {
        super(Language.translate("craft.screen.world_selection"));
    }

    @Override
    public void show() {
        super.show();

        this.list = this.add(new SelectionList<>(0, 70, this.width, this.height - 70, 14));
        this.list.setSelectable(true);
        this.list.setItemRenderer(this::renderItem);

        this.list.addEntries(SaveManager.reloadWorlds());

        this.openButton = this.add(new Button(this.width / 2 - 100, 40, 60, Language.translate("craft.screen.world_selection.open"), this::openWorld));
        this.modifyButton = this.add(new Button(this.width / 2 - 30, 40, 60, Language.translate("craft.screen.world_selection.modify"), this::modifyWorld));
        this.createButton = this.add(new Button(this.width / 2 + 40, 40, 60, Language.translate("craft.screen.world_selection.create"), this::createWorld));
        this.createButton.setColor(Color.rgb(0x00cf00));
        this.deleteButton = this.add(new Button(this.width / 2 - 100, 10, 95, Language.translate("craft.screen.world_selection.delete"), this::deleteWorld));
        this.deleteButton.setColor(Color.rgb(0xff0000));
        this.backButton = this.add(new Button(this.width / 2 + 5, 10, 95, CommonTexts.back(), caller -> this.back()));
    }

    private void deleteWorld(Button caller) {
        SavedWorld toDelete = this.list.getSelected();

        String name;
        try {
            name = toDelete.getWorldInfo().getName();
        } catch (IOException e) {
            name = toDelete.getDirectory().name();
        }

        ConfirmationScreen confirmationScreen = new ConfirmationScreen(Language.translate("craft.screen.world_delete.title"), Language.translate("craft.screen.world_delete.description", name), $ -> toDelete.delete());
        this.game.showScreen(confirmationScreen);
    }

    private void openWorld(Button caller) {
        SavedWorld selected = this.list.getSelected();
        this.game.loadWorld(selected);
    }

    private void modifyWorld(Button caller) {

    }

    private void createWorld(Button caller) {
        this.game.showScreen(new CreateWorldScreen());
    }

    private void renderItem(Renderer renderer, SavedWorld world, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        try {
            renderer.drawCenteredText(world.getWorldInfo().getName(), (int)(float)(this.width - 120), 11f);
        } catch (IOException e) {
            e.printStackTrace();
            renderer.drawCenteredText(e.toString(), (int)(float)(this.width / 2), 11f, Color.rgb(0xff404040));
        }
    }

    public Button getBackButton() {
        return this.backButton;
    }

    public Button getOpenButton() {
        return this.openButton;
    }

    public Button getModifyButton() {
        return this.modifyButton;
    }

    public Button getCreateButton() {
        return this.createButton;
    }

    public Button getDeleteButton() {
        return this.deleteButton;
    }

    public SelectionList<SavedWorld> getList() {
        return this.list;
    }

    public GlyphLayout getLayout() {
        return this.layout;
    }
}
