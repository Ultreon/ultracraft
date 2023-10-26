package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.network.packets.c2s.C2SRespawnPacket;
import com.ultreon.libs.translations.v1.Language;

public class DeathScreen extends Screen {
    private Button respawnButton;
    private Button exitWorldButton;

    public DeathScreen() {
        super(Language.translate("craft.screen.death.title"));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        this.respawnButton.setPos(width / 2 - 100, height / 3 + 5);
        this.exitWorldButton.setPos(width / 2 - 100, height / 3 - 25);
    }

    @Override
    public void init() {
        super.init();

        this.respawnButton = add(new Button(this.width / 2 - 100, this.height / 3 + 5, 200, Language.translate("craft.screen.death.respawn"), this::respawn));
        this.exitWorldButton = add(new Button(this.width / 2 - 100, this.height / 3 - 25, 200, Language.translate("craft.screen.pause.exit_world"), this::exitWorld));
        this.exitWorldButton.setColor(Color.RED);
        this.exitWorldButton.setTextColor(Color.WHITE);
    }

    private void respawn(Button button) {
        this.client.connection.send(new C2SRespawnPacket());
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.drawTextScaled(this.title, 2, (int)((float) this.width / 2 - this.font.width(this.title) / 2) / 2, (int)((float) (this.height + 40) / 2));
    }

    private void exitWorld(Button caller) {
        this.client.exitWorldToTitle();
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
