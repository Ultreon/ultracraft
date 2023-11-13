package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.util.Color;
import org.jetbrains.annotations.NotNull;

public class DisconnectedScreen extends Screen {
    private final String message;

    public DisconnectedScreen(String message) {
        super("Disconnected");
        this.message = message;
    }

    @Override
    public void build(GuiBuilder builder) {
        var backButton = builder.addWithPos(new Button(150), () -> new Position(this.size.width / 2 - 75, this.size.height / 2 - 10));
        backButton.callback().set(caller -> new TitleScreen());
        backButton.text().translate("ultracraft.ui.backToTitle");
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
        renderer.drawTextScaledCenter(this.title, 2, this.size.width / 2, this.size.height / 3, Color.WHITE);

        int lineY = 0;
        for (String line : this.message.lines().toList()) {
            renderer.drawTextCenter(line, this.size.width / 2, this.size.height / 3 + 30 + lineY * (this.font.lineHeight + 1) - 1, Color.WHITE, false);
            lineY++;
        }
    }
}
