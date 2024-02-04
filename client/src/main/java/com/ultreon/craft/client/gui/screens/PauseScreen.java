package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen;
import com.ultreon.craft.client.gui.screens.settings.SettingsScreen;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.input.DesktopInput;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.ServerWorld;

public class PauseScreen extends Screen {
    private TextButton backToGameButton;
    private TextButton optionsButton;
    private TextButton exitWorldButton;

    public PauseScreen() {
        super("Game Paused");
    }

    @Override
    public void build(GuiBuilder builder) {
        IntegratedServer server = this.client.getSingleplayerServer();
        ServerWorld world = server != null ? server.getWorld() : null;
        if (world != null)
            this.client.addFuture(world.saveAsync(false));

        this.backToGameButton = builder.add(TextButton.of(TextObject.translation("ultracraft.ui.backToGame"))
                        .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 3 - 25)))
                .callback(this::resumeGame);

        this.optionsButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.options"), 95)
                        .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 3)))
                .callback(caller -> UltracraftClient.get().showScreen(new SettingsScreen()));

        this.exitWorldButton = builder.add(TextButton.of(TextObject.translation("ultracraft.ui.exitWorld"), 95)
                        .position(() -> new Position(this.size.width / 2 + 5, this.size.height / 3)))
                .callback(this::exitWorld);
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    public TextButton getBackToGameButton() {
        return this.backToGameButton;
    }

    public TextButton getOptionsButton() {
        return this.optionsButton;
    }

    public TextButton getExitWorldButton() {
        return this.exitWorldButton;
    }

    private void exitWorld(TextButton caller) {
        this.client.exitWorldToTitle();
    }

    private void resumeGame(TextButton caller) {
        this.client.resume();
    }
}
