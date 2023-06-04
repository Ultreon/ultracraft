package com.ultreon.craft.render.gui.screens;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.craft.world.World;
import com.ultreon.libs.translations.v0.Language;

import java.io.IOException;

public class PauseScreen extends Screen {
    private Button backToGameButton;
    private Button optionsButton;
    private Button exitWorldButton;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        this.backToGameButton.setPos(width / 2 - 100, height - height / 3 + 5);
        this.optionsButton.setPos(width / 2 - 100, height - height / 3 - 25);
        this.exitWorldButton.setPos(width / 2 + 5, height - height / 3 - 25);
    }

    @Override
    public void show() {
        super.show();

        try {
            World world = this.game.world;
            if (world != null) {
                this.game.addFuture(world.saveAsync());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.backToGameButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 + 5, 200, Language.translate("craft/screen/pause/back_to_game"), this::resumeGame));
        this.optionsButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 - 25, 95, Language.translate("craft/screen/title/options"), caller -> {
            UltreonCraft.get().showScreen(new LanguageScreen());
        }));
        this.exitWorldButton = add(new Button(this.width / 2 + 5, this.height - this.height / 3 - 25, 95, Language.translate("craft/screen/pause/exit_world"), this::exitWorld));
        this.exitWorldButton.setColor(Color.red);
        this.exitWorldButton.setTextColor(Color.white);
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
        this.game.exitWorld();
    }

    private void resumeGame(Button caller) {
        this.game.resume();
    }
}
