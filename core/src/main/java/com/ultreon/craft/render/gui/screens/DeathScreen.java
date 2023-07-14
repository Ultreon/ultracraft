package com.ultreon.craft.render.gui.screens;

import com.ultreon.craft.Task;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.translations.v1.Language;

public class DeathScreen extends Screen {
    private Button respawnButton;
    private Button exitWorldButton;

    public DeathScreen() {
        super(Language.translate("craft.screen.death.title"));
    }

    @Override
    public void show() {
        super.show();

        this.respawnButton = this.add(new Button(this.width / 2 - 100, this.height - this.height / 3 + 5, 200, Language.translate("craft.screen.death.respawn"), this::respawn));
        this.exitWorldButton = this.add(new Button(this.width / 2 - 100, this.height - this.height / 3 - 25, 200, Language.translate("craft.screen.pause.exit_world"), this::exitWorld));
        this.exitWorldButton.setColor(Color.RED);
        this.exitWorldButton.setTextColor(Color.WHITE);
    }

    private void respawn(Button button) {
        this.game.respawnAsync().thenAccept(unused -> {
            this.game.runLater(new Task(new Identifier("post_respawn"), () -> {
                this.game.showScreen(null);
            }));
        });
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.drawTextScaled(this.title, 2, (int)((float) this.width / 2 - this.font.width(this.title) / 2) / 2, (int)((float) (this.height - 40) / 2));
    }

    private void exitWorld(Button caller) {
        this.game.exitWorldToTitle();
    }

    public Button getRespawnButton() {
        return this.respawnButton;
    }

    public Button getExitWorldButton() {
        return this.exitWorldButton;
    }

    @Override
    public boolean canClose() {
        return false;
    }
}
