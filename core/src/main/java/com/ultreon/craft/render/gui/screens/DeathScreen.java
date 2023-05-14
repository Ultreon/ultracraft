package com.ultreon.craft.render.gui.screens;

import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.gui.widget.Button;

public class DeathScreen extends Screen {
    private Button respawnButton;
    private Button exitWorldButton;

    public DeathScreen() {
        super("You Died");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        this.respawnButton.setPos(width / 2 - 100, height - height / 3 + 5);
        this.exitWorldButton.setPos(width / 2 - 100, height - height / 3 - 25);
    }

    @Override
    public void show() {
        super.show();

        this.respawnButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 + 5, 200, "Respawn", this::respawn));
        this.exitWorldButton = add(new Button(this.width / 2 - 100, this.height - this.height / 3 - 25, 200, "Exit World", this::exitWorld));
        this.exitWorldButton.setColor(Color.red);
        this.exitWorldButton.setTextColor(Color.white);
    }

    private void respawn(Button button) {
        this.game.respawn();
        this.game.showScreen(null);
    }

    private void exitWorld(Button caller) {
        this.game.exitWorld();
    }

    public Button getRespawnButton() {
        return respawnButton;
    }

    public Button getExitWorldButton() {
        return exitWorldButton;
    }
}
