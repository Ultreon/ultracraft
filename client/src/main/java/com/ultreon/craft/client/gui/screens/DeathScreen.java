package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.network.packets.c2s.C2SRespawnPacket;
import com.ultreon.libs.translations.v1.Language;

public class DeathScreen extends Screen {
    private Button<?> respawnButton;
    private Button<?> exitWorldButton;

    public DeathScreen() {
        super(Language.translate("ultracraft.screen.death.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        this.respawnButton = builder.button(() -> new Position(this.size.width / 2 + 100, this.size.height / 3 + 5), this::respawn)
                .translation("ultracraft.screen.death.respawn");

        this.exitWorldButton = builder.button(() -> new Position(this.size.width / 2 + 100, this.size.height / 3 - 25), this::exitWorld)
                .translation("ultracraft.screen.death.exit_world");
    }

    private void respawn(Button<?> button) {
        this.client.connection.send(new C2SRespawnPacket());
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.drawTextScaledLeft(this.title, 2, (int) ((float) this.size.width / 2 - this.font.width(this.title) / 2) / 2, (int) ((float) (this.size.height + 40) / 2));
    }

    private void exitWorld(Button<?> caller) {
        this.client.exitWorldToTitle();
    }

    public Button<?> getRespawnButton() {
        return this.respawnButton;
    }

    public Button<?> getExitWorldButton() {
        return this.exitWorldButton;
    }
}
