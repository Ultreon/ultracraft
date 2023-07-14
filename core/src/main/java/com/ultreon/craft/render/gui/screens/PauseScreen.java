package com.ultreon.craft.render.gui.screens;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.craft.world.World;
import com.ultreon.libs.translations.v1.Language;

public class PauseScreen extends Screen {
    private Button backToGameButton;
    private Button optionsButton;
    private Button exitWorldButton;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    public void show() {
        super.show();

        World world = this.game.world;
        if (world != null) {
            this.game.addFuture(world.saveAsync(false));
        }

        this.backToGameButton = this.add(new Button(this.width / 2 - 100, this.height - this.height / 3 + 5, 200, Language.translate("craft.screen.pause.back_to_game"), this::resumeGame));
        this.optionsButton = this.add(new Button(this.width / 2 - 100, this.height - this.height / 3 - 25, 95, Language.translate("craft.screen.title.options"), caller -> {
            UltreonCraft.get().showScreen(new LanguageScreen());
        }));
        this.exitWorldButton = this.add(new Button(this.width / 2 + 5, this.height - this.height / 3 - 25, 95, Language.translate("craft.screen.pause.exit_world"), this::exitWorld));
        this.exitWorldButton.setColor(Color.RED);
        this.exitWorldButton.setTextColor(Color.WHITE);
    }

    public Button getBackToGameButton() {
        return backToGameButton;
    }

    public Button getOptionsButton() {
        return optionsButton;
    }

    public Button getExitWorldButton() {
        return exitWorldButton;
    }

    private void exitWorld(Button caller) {
        this.game.exitWorldToTitle();
    }

    private void resumeGame(Button caller) {
        this.game.resume();
    }
}
