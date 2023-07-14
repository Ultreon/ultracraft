package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.GameFlags;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.screens.world.WorldSelectionScreen;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.translations.v1.Language;

import java.util.concurrent.TimeUnit;

public class TitleScreen extends Screen {
    private Button startButton;
    private Button resetWorldButton;
    private Button modListButton;
    private Button optionsButton;
    private Button quitButton;
    private static int hiddenClicks = 0;

    public TitleScreen() {
        super("Title Screen");
    }

    @Override
    public void show() {
        super.show();

        int y = this.height - this.height / 2 + 5;

        this.startButton = this.add(new Button(this.width / 2 - 100, y, 200, Language.translate("craft.screen.title.world_selection"), caller -> this.game.showScreen(new WorldSelectionScreen())));

        if ((GamePlatform.instance.isMobile() && GameFlags.ENABLE_RESET_WORLD_IN_MOBILE) ||
                (GamePlatform.instance.isDesktop() && GameFlags.ENABLE_RESET_WORLD_IN_DESKTOP) ||
                (GamePlatform.instance.isWeb() && GameFlags.ENABLE_RESET_WORLD_IN_WEB) ||
                GameFlags.ALWAYS_ENABLE_RESET_WORLD) {
            this.resetWorldButton = this.add(new Button(this.width / 2 - 100, y-=25, 200, Language.translate("craft.screen.title.reset_world"), caller -> UltreonCraft.getSavedWorld().delete()));
        }

        if (GamePlatform.instance.isModsSupported()) {
            this.modListButton = this.add(new Button(this.width / 2 - 100, y -= 25, 200, Language.translate("craft.screen.mod_list"), caller -> GamePlatform.instance.openModList()));
        }
        this.optionsButton = this.add(new Button(this.width / 2 - 100, y-=25, 95, Language.translate("craft.screen.title.options"), caller -> UltreonCraft.get().showScreen(new LanguageScreen())));
        this.quitButton = new Button(this.width / 2 + 5, y, 95, Language.translate("craft.screen.title.quit"), caller -> {
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

        renderer.drawCenteredTextScaled("UltraCraft", 3, (int)((float) this.width / 2), (int)((float) (this.height - 40)));
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        if (!GamePlatform.instance.supportsQuit() && this.quitButton.isWithinBounds(x, y)) {
            hiddenClicks++;
            if (hiddenClicks == 1) {
                this.game.schedule(new Task(new Identifier("button_crash"), () -> hiddenClicks = 0), 2, TimeUnit.MINUTES);
            }

            UltreonCraft.LOGGER.warn("Clicks: " + hiddenClicks);
            if (hiddenClicks == 16) {
                CrashLog crashLog = new CrashLog("Hidden quit button", new Exception("Funny"));
                this.game.delayCrash(crashLog);
            }
        }
        return super.mouseClick(x, y, button, count);
    }

    public Button getStartButton() {
        return this.startButton;
    }

    public Button getResetWorldButton() {
        return this.resetWorldButton;
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
}
