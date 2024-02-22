package com.ultreon.craft.client;

import com.ultreon.craft.api.ubo.UboFormatter;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.network.PacketOverflowException;
import com.ultreon.craft.text.FormattedText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.data.types.MapType;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DevScreen extends Screen {
    private final TextObject formatted;

    protected DevScreen() {
        super(TextObject.translation("ultracraft.screen.dev"));

        // Create an example map
        MapType exampleMap = Utils.createExampleMap();
        this.formatted = UboFormatter.format(exampleMap);
    }

    @Override
    public void build(GuiBuilder builder) {
//        builder.add(Label.of(TextObject.translation("ultracraft.screen.dev.message"))
//                .alignment(Alignment.LEFT))
//                .position(() -> new Position(40, 40));
//
//        builder.add(TextButton.of(TextObject.translation("ultracraft.screen.dev.close"))
//                .bounds(() -> new Bounds(client.getScaledWidth() / 2 - 50, client.getScaledHeight() - 40, 100, 20)))
//                .callback(caller -> this.close());
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        renderBackground(renderer);

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

//        List<FormattedText> lines = this.font.wordWrap(formatted, this.size.width - 20);
        renderer.textLeft(formatted, 10, 10);
    }
}
