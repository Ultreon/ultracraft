package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Panel;
import com.ultreon.craft.client.rpc.Activity;
import com.ultreon.craft.client.util.Resizer;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec2f;

public class TitleScreen extends Screen {
    private Label titleLabel;
    private Button singleplayerButton;
    private Button multiplayerButton;
    private Button modListButton;
    private Button optionsButton;
    private Button quitButton;
    private final Resizer resizer;

    public TitleScreen() {
        super(TextObject.translation("ultracraft.screen.title"));

        this.resizer = new Resizer(7680, 4320);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.client.setActivity(Activity.MAIN_MENU);

        Panel panel = builder.addWithBounds(new Panel(), () -> new Bounds(0, 0, 250, this.size.height));
        panel.backgroundColor().set(Color.BLACK.withAlpha(0x80));

        this.titleLabel = builder.addWithPos(new Label(), () -> new Position(125, 40));
        this.titleLabel.text().set(TextObject.literal("Ultracraft").setBold(true));
        this.titleLabel.alignment().set(Alignment.CENTER);
        this.titleLabel.scale().set(3);

        this.singleplayerButton = builder.addWithPos(new Button(150), () -> new Position(50, this.size.height / 2 - 35));
        this.singleplayerButton.callback().set(this::openSingleplayer);
        this.singleplayerButton.text().translate("ultracraft.screen.title.singleplayer");

        this.multiplayerButton = builder.addWithPos(new Button(150), () -> new Position(50, this.size.height / 2 - 10));
        this.multiplayerButton.callback().set(this::openMultiplayer);
        this.multiplayerButton.text().translate("ultracraft.screen.multiplayer");

        this.modListButton = builder.addWithPos(new Button(150), () -> new Position(50, this.size.height / 2 + 15));
        this.modListButton.callback().set(this::showModList);
        this.modListButton.text().translate("ultracraft.screen.mod_list");

        this.optionsButton = builder.addWithPos(new Button(150), () -> new Position(50, this.size.height / 2 + 40));
        this.optionsButton.callback().set(this::showOptions);
        this.optionsButton.text().translate("ultracraft.screen.options");

        this.quitButton = builder.addWithPos(new Button(150), () -> new Position(50, this.size.height / 2 + 78));
        this.quitButton.callback().set(this::quitGame);
        this.quitButton.text().translate("ultracraft.screen.title.quit");
    }

    private void quitGame(Button caller) {
        Gdx.app.exit();
    }

    private void openSingleplayer(Button caller) {
        this.client.showScreen(new WorldSelectionScreen());
    }

    private void openMultiplayer(Button caller) {
        this.client.showScreen(new MultiplayerScreen());
    }

    private void showOptions(Button caller) {
        this.client.showScreen(new OptionsScreen());
    }

    private void showModList(Button caller) {
        this.client.showScreen(new ModListScreen(this));
    }

    @Override
    protected void renderSolidBackground(Renderer renderer) {
        super.renderSolidBackground(renderer);

        Vec2f thumbnail = this.resizer.thumbnail(this.size.width, this.size.height);

        float drawWidth = thumbnail.x;
        float drawHeight = thumbnail.y;

        float drawX = (this.size.width - drawWidth) / 2;
        float drawY = (this.size.height - drawHeight) / 2;
        renderer.blit(UltracraftClient.id("textures/gui/title_background.png"), (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, this.resizer.getSourceWidth(), this.resizer.getSourceHeight(), (int) this.resizer.getSourceWidth(), (int) this.resizer.getSourceHeight());
    }

    public Button getSingleplayerButton() {
        return this.singleplayerButton;
    }

    public Button getMultiplayerButton() {
        return this.multiplayerButton;
    }

    public Button getModListButton() {
        return this.modListButton;
    }

    public Button getOptionsButton() {
        return this.optionsButton;
    }

    public Button getQuitButton() {
        return this.quitButton;
    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public boolean onClose(Screen next) {
        return !(next instanceof TitleScreen);
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }
}
