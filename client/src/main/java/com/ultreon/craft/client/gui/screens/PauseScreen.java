package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.world.ServerWorld;

public class PauseScreen extends Screen {
    private Button<?> backToGameButton;
    private Button<?> optionsButton;
    private Button<?> exitWorldButton;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    public void build(GuiBuilder builder) {
        IntegratedServer server = this.client.getSingleplayerServer();
        ServerWorld world = server != null ? server.getWorld() : null;
        if (world != null)
            this.client.addFuture(world.saveAsync(false));

        this.backToGameButton = builder.button(() -> new Position(this.size.width / 2 - 100, this.size.height / 3 - 25), this::resumeGame)
                .translation("ultracraft.ui.backToGame");

        this.optionsButton = builder.button(() -> new Position(this.size.width / 2 - 100, this.size.height / 3), caller -> UltracraftClient.get().showScreen(new LanguageScreen()))
                .width(95)
                .translation("ultracraft.screen.title.options");

        this.exitWorldButton = builder.button(() -> new Position(this.size.width / 2 + 5, this.size.height / 3), this::exitWorld)
                .width(95)
                .color(Color.RED)
                .textColor(Color.WHITE)
                .translation("ultracraft.screen.pause.exit_world");
    }

    public Button<?> getBackToGameButton() {
        return this.backToGameButton;
    }

    public Button<?> getOptionsButton() {
        return this.optionsButton;
    }

    public Button<?> getExitWorldButton() {
        return this.exitWorldButton;
    }

    private void exitWorld(Button<?> caller) {
        this.client.exitWorldToTitle();
    }

    private void resumeGame(Button<?> caller) {
        this.client.resume();
    }
}
