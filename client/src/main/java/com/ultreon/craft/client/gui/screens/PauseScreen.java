package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.input.DesktopInput;
import com.ultreon.craft.world.ServerWorld;

public class PauseScreen extends Screen {
    private Button backToGameButton;
    private Button optionsButton;
    private Button exitWorldButton;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    public void build(GuiBuilder builder) {
        IntegratedServer server = this.client.getSingleplayerServer();
        ServerWorld world = server != null ? server.getWorld() : null;
        if (world != null)
            this.client.addFuture(world.saveAsync(false));

        this.backToGameButton = builder.addWithPos(new Button(), () -> new Position(this.size.width / 2 - 100, this.size.height / 3 - 25));
        this.backToGameButton.callback().set(this::resumeGame);
        this.backToGameButton.text().translate("ultracraft.ui.backToGame");

        this.optionsButton = builder.addWithPos(new Button(95), () -> new Position(this.size.width / 2 - 100, this.size.height / 3));
        this.optionsButton.callback().set(caller -> UltracraftClient.get().showScreen(new OptionsScreen()));
        this.optionsButton.text().translate("ultracraft.screen.options");

        this.exitWorldButton = builder.addWithPos(new Button(95), () -> new Position(this.size.width / 2 + 5, this.size.height / 3));
        this.exitWorldButton.callback().set(this::exitWorld);
        this.exitWorldButton.text().translate("ultracraft.screen.pause.exit_world");

        if (DesktopInput.PAUSE_KEY.isJustPressed() && Gdx.input.isCursorCatched()) {
            this.client.showScreen(null);
        }
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
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
