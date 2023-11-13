package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.network.packets.c2s.C2SRespawnPacket;
import com.ultreon.libs.translations.v1.Language;

public class DeathScreen extends Screen {
    private Button respawnButton;
    private Button exitWorldButton;

    public DeathScreen() {
        super(Language.translate("ultracraft.screen.death.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        var titleLabel = builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.size.width / 2, this.size.height / 2 - 50));
        titleLabel.text().set(this.title);
        this.respawnButton = builder.addWithPos(new Button(), () -> new Position(this.size.width / 2 - 100, this.size.height / 3));
        this.respawnButton.callback().set(this::respawn);
        this.respawnButton.text().translate("ultracraft.screen.death.respawn");

        this.exitWorldButton = builder.addWithPos(new Button(), () -> new Position(this.size.width / 2 - 100, this.size.height / 3 + 25));
        this.exitWorldButton.callback().set(this::exitWorld);
        this.exitWorldButton.text().translate("ultracraft.ui.exitWorld");
    }

    private void respawn(Button button) {
        this.client.connection.send(new C2SRespawnPacket());
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);
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
}
