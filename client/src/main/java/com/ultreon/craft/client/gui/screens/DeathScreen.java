package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.Language;
import com.ultreon.craft.network.packets.c2s.C2SRespawnPacket;
import com.ultreon.craft.text.TextObject;

public class DeathScreen extends Screen {
    private TextButton respawnButton;
    private TextButton exitWorldButton;

    public DeathScreen() {
        super(Language.translate("ultracraft.screen.death.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 2 - 50)));

        this.respawnButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.death.respawn"))
                .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 3))
                .callback(this::respawn)
                .translation("ultracraft.screen.death.respawn"));

        this.exitWorldButton = builder.add(TextButton.of(TextObject.translation("ultracraft.ui.exitWorld"))
                        .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 3 + 25)))
                .callback(this::exitWorld)
                .translation("ultracraft.ui.exitWorld");
    }

    private void respawn(TextButton button) {
        this.client.connection.send(new C2SRespawnPacket());
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);
    }

    private void exitWorld(TextButton caller) {
        this.client.exitWorldToTitle();
    }

    public TextButton getRespawnButton() {
        return this.respawnButton;
    }

    public TextButton getExitWorldButton() {
        return this.exitWorldButton;
    }
}
