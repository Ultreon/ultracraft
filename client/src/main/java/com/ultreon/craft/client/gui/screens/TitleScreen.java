package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screen.ModListScreen;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.rpc.Activity;
import com.ultreon.libs.translations.v1.Language;
import net.miginfocom.layout.AC;

import java.io.IOException;

public class TitleScreen extends Screen {
    private Button startButton;
    private Button resetWorldButton;
    private Button modListButton;
    private Button optionsButton;
    private Button quitButton;
    private final GlyphLayout layout = new GlyphLayout();
    private static int hiddenClicks = 0;
    private boolean activitySet = false;

    public TitleScreen() {
        super("Title Screen");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        int y = height / 2 + 5;
        this.startButton.setPos(width / 2 - 100, y);
        if (this.resetWorldButton != null) {
            this.resetWorldButton.setPos(width / 2 - 100, y += 25);
        }
        if (this.modListButton != null) {
            this.modListButton.setPos(width / 2 - 100, y += 25);
        }
        this.optionsButton.setPos(width / 2 - 100, y += 25);
        this.quitButton.setPos(width / 2 + 5, y);
    }

    @Override
    public void init() {
        this.clearWidgets();

        super.init();

        if (!this.activitySet) {
            this.client.setActivity(Activity.MAIN_MENU);
        }

        int y = this.height / 2 + 5;

        this.startButton = this.add(new Button(this.width / 2 - 100, y, 200, Language.translate("craft.screen.title.start_world"), caller -> {
            try {
                UltracraftClient.getSavedWorld().delete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            UltracraftClient.get().startWorld();
        }));

        this.modListButton = this.add(new Button(this.width / 2 - 100, y += 25, 200, Language.translate("craft.screen.mod_list"), caller -> {
            this.client.showScreen(new ModListScreen(this));
        }));
        this.optionsButton = this.add(new Button(this.width / 2 - 100, y += 25, 95, Language.translate("craft.screen.title.options"), caller -> {
            this.client.showScreen(new LanguageScreen());
        }));
        this.quitButton = this.add(new Button(this.width / 2 + 5, y, 95, Language.translate("craft.screen.title.quit"), caller -> {
            Gdx.app.exit();
        }));
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.drawCenteredTextScaled("Ultracraft", 3, (int) ((float) this.width / 2), (int) (float) 40);
    }

    public Button getStartButton() {
        return this.startButton;
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
}
