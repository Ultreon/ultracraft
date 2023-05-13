package com.ultreon.craft.render.gui.screens;

import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.gui.widget.Button;

public class PauseScreen extends Screen {
    private Button backToGameButton;
    private Button exitWorldButton;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    public void show() {
        super.show();

        this.backToGameButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 + 5, 200, "Back to the Game", this::resumeGame));
        this.exitWorldButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 - 25, 200, "Exit World", this::exitWorld));
        this.exitWorldButton.setColor(Color.red);
        this.exitWorldButton.setTextColor(Color.white);
    }

    public Button getBackToGameButton() {
        return backToGameButton;
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
