package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.client.GamePlatform;
import com.ultreon.craft.util.Task;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.translations.v1.Language;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TitleScreen extends Screen {
    private Button startButton;
    private Button resetWorldButton;
    private Button modListButton;
    private Button optionsButton;
    private Button quitButton;
    private final GlyphLayout layout = new GlyphLayout();
    private static int hiddenClicks = 0;

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
        clearWidgets();

        super.init();

        int y = height / 2 + 5;

        startButton = add(new Button(width / 2 - 100, y, 200, Language.translate("craft.screen.title.start_world"), caller -> {
            try {
                UltracraftClient.getSavedWorld().delete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            UltracraftClient.get().startWorld();
        }));

        this.resetWorldButton = this.add(new Button(this.width / 2 - 100, y+=25, 200, Language.translate("craft.screen.title.reset_world"), caller -> {
            try {
                UltracraftClient.getSavedWorld().delete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        if (GamePlatform.instance.isModsSupported()) {
            this.modListButton = this.add(new Button(this.width / 2 - 100, y += 25, 200, Language.translate("craft.screen.mod_list"), caller -> {
                GamePlatform.instance.openModList();
            }));
        }
        optionsButton = add(new Button(width / 2 - 100, y+=25, 95, Language.translate("craft.screen.title.options"), caller -> {
            UltracraftClient.get().showScreen(new LanguageScreen());
        }));
        this.quitButton = new Button(width / 2 + 5, y, 95, Language.translate("craft.screen.title.quit"), caller -> {
            if (GamePlatform.instance.supportsQuit()) {
                Gdx.app.exit();
            }
        });

        if (GamePlatform.instance.supportsQuit()) {
            this.add(this.quitButton);
        } else {
            this.optionsButton.setWidth(200);
        }
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.drawCenteredTextScaled("Ultracraft", 3, (int)((float) this.width / 2), (int) (float) 40);
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        if (!GamePlatform.instance.supportsQuit() && this.quitButton.isWithinBounds(x, y)) {
            hiddenClicks++;
            if (hiddenClicks == 1) {
                this.client.schedule(new Task(new Identifier("button_crash"), () -> {
                    hiddenClicks = 0;
                }), 2, TimeUnit.MINUTES);
            }

            UltracraftClient.LOGGER.warn("Clicks: " + hiddenClicks);
            if (hiddenClicks == 16) {
                CrashLog crashLog = new CrashLog("Hidden quit button", new Exception("Funny"));
                this.client.delayCrash(crashLog);
            }
        }
        return super.mouseClick(x, y, button, count);
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getOptionsButton() {
        return optionsButton;
    }

    public Button getQuitButton() {
        return quitButton;
    }

    @Override
    public boolean canClose() {
        return false;
    }
}
