package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.translations.v1.Language;

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

        this.backToGameButton.setPos(width / 2 - 100, height / 3 + 5);
        this.optionsButton.setPos(width / 2 - 100, height / 3 - 25);
        this.exitWorldButton.setPos(width / 2 + 5, height / 3 - 25);
    }

    @Override
    public void init() {
        super.init();

        IntegratedServer server = this.client.getSingleplayerServer();
        ServerWorld world = server != null ? server.getWorld() : null;
        if (world != null)
            this.client.addFuture(world.saveAsync(false));

        this.backToGameButton = this.add(new Button(this.width / 2 - 100, this.height / 3 - 25, 200, Language.translate("craft.screen.pause.back_to_game"), this::resumeGame));
        this.optionsButton = this.add(new Button(this.width / 2 - 100, this.height / 3, 95, Language.translate("craft.screen.title.options"), caller -> UltracraftClient.get().showScreen(new LanguageScreen())));
        this.exitWorldButton = this.add(new Button(this.width / 2 + 5, this.height / 3, 95, Language.translate("craft.screen.pause.exit_world"), this::exitWorld));
        this.exitWorldButton.setColor(Color.RED);
        this.exitWorldButton.setTextColor(Color.WHITE);
    }

    public Button getBackToGameButton() {
        return this.backToGameButton;
    }

    public Button getOptionsButton() {
        return this.optionsButton;
    }

    public Button getExitWorldButton() {
        return this.exitWorldButton;
    }

    private void exitWorld(Button caller) {
        this.client.exitWorldToTitle();
    }

    private void resumeGame(Button caller) {
        this.client.resume();
    }
}
