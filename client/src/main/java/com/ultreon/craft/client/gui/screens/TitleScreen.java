package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.rpc.Activity;
import com.ultreon.craft.client.util.Resizer;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec2f;

public class TitleScreen extends Screen {
    private Label titleLabel;
    private Button<?> singleplayerButton;
    private Button<?> multiplayerButton;
    private Button<?> modListButton;
    private Button<?> optionsButton;
    private Button<?> quitButton;
    private final Resizer resizer;

    public TitleScreen() {
        super(TextObject.translation("ultracraft.screen.title"));

        this.resizer = new Resizer(7680, 4320);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.client.setActivity(Activity.MAIN_MENU);

        builder.panelBounds(() -> new Bounds(0, 0, 250, this.size.height))
                .backgroundColor(Color.BLACK.withAlpha(0x80));

        this.titleLabel = builder.label(() -> new Position(125, 40)).text(TextObject.literal("Ultracraft").setBold(true)).alignment(Alignment.CENTER).scale(3);

        this.singleplayerButton = builder.button(() -> new Position(50, this.size.height / 2 - 35), this::openSingleplayer)
                .translation("ultracraft.screen.title.singleplayer")
                .width(150);

        this.multiplayerButton = builder.button(() -> new Position(50, this.size.height / 2 - 10), this::openMultiplayer)
                .translation("ultracraft.screen.multiplayer")
                .width(150);

        this.modListButton = builder.button(() -> new Position(50, this.size.height / 2 + 15), this::showModList)
                .translation("ultracraft.screen.mod_list")
                .width(150);

        this.optionsButton = builder.button(() -> new Position(50, this.size.height / 2 + 40), this::showOptions)
                .translation("ultracraft.screen.title.options")
                .width(150);

        this.quitButton = builder.button(() -> new Position(50, this.size.height / 2 + 78), this::quitGame)
                .translation("ultracraft.screen.title.quit")
                .width(150);
    }

    private void quitGame(Button<?> caller) {
        Gdx.app.exit();
    }

    private void openSingleplayer(Button<?> caller) {
        this.client.startWorld();
    }

    private void openMultiplayer(Button<?> caller) {
        this.client.showScreen(new MultiplayerScreen());
    }

    private void showOptions(Button<?> caller) {
        this.client.showScreen(new LanguageScreen());
    }

    private void showModList(Button<?> caller) {
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

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

//        renderer.drawTextScaledCenter("Ultracraft", 3, (int) ((float) 125), (int) (float) 40);
    }

    public Button<?> getSingleplayerButton() {
        return this.singleplayerButton;
    }

    public Button<?> getMultiplayerButton() {
        return this.multiplayerButton;
    }

    public Button<?> getModListButton() {
        return this.modListButton;
    }

    public Button<?> getOptionsButton() {
        return this.optionsButton;
    }

    public Button<?> getQuitButton() {
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
