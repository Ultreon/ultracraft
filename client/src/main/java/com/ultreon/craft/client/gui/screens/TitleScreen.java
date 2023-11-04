package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Panel;
import com.ultreon.craft.client.rpc.Activity;
import com.ultreon.craft.client.util.Resizer;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.translations.v1.Language;

import java.io.IOException;

public class TitleScreen extends Screen {
    private Button singleplayerButton;

    private Button optionsButton;
    private Button quitButton;
    private final Resizer resizer;
    private boolean activitySet = false;

    public TitleScreen() {
        super("Title Screen");

        this.resizer = new Resizer(7680, 4320);
    }

    private static void quitGame(Button caller) {
        Gdx.app.exit();
    }

    private void openSingleplayer(Button caller) {
        try {
            UltracraftClient.getSavedWorld().delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.client.startWorld();
    }

    private void openMultiplayer(Button caller) {
        this.client.showScreen(new MultiplayerScreen());
    }

    @Override
    public void init() {
        this.clearWidgets();

        super.init();

        if (!this.activitySet) {
            this.client.setActivity(Activity.MAIN_MENU);
        }

        int y = this.height / 2 - 35;

        this.add(new Panel(0, 0, 250, this.height)).setBackgroundColor(0x80000000);
        this.singleplayerButton = this.add(new Button(50, y, 150, Language.translate("ultracraft.screen.title.singleplayer"), this::openSingleplayer));
        this.add(new Button(50, y += 25, 150, Language.translate("ultracraft.screen.multiplayer"), this::openMultiplayer));
        this.add(new Button(50, y += 25, 150, Language.translate("ultracraft.screen.mod_list"), this::showModList));
        this.optionsButton = this.add(new Button(50, y += 25, 150, Language.translate("ultracraft.screen.title.options"), this::showOptions));
        this.quitButton = this.add(new Button(50, y += 25, 150, Language.translate("ultracraft.screen.title.quit"), TitleScreen::quitGame));
    }

    @Override
    protected void renderSolidBackground(Renderer renderer) {
        super.renderSolidBackground(renderer);

        Vec2f thumbnail = this.resizer.thumbnail(this.width, this.height);

        float drawWidth = thumbnail.x;
        float drawHeight = thumbnail.y;

        float drawX = (this.width - drawWidth) / 2;
        float drawY = (this.height - drawHeight) / 2;
        renderer.blit(UltracraftClient.id("textures/gui/title_background.png"), (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, this.resizer.getSourceWidth(), this.resizer.getSourceHeight(), (int) this.resizer.getSourceWidth(), (int) this.resizer.getSourceHeight());
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.render(renderer, mouseX, mouseY, deltaTime);

        renderer.drawCenteredTextScaled("Ultracraft", 3, (int) ((float) 125), (int) (float) 40);
    }

    public Button getSingleplayerButton() {
        return this.singleplayerButton;
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

    private void showOptions(Button caller) {
        this.client.showScreen(new LanguageScreen());
    }

    private void showModList(Button caller) {
        this.client.showScreen(new ModListScreen(this));
    }
}
