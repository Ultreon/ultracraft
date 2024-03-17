package com.ultreon.craft.client;

import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

/**
 * Development preview screen. Shows up when the game is still in development.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class DevScreen extends Screen {
    /**
     * Constructor for DevScreen class.
     * Initializes with a translation text for the screen title.
     */
    protected DevScreen() {
        super(TextObject.translation("ultracraft.screen.dev"));
    }

    /**
     * Builds the GUI layout for the DevScreen.
     * Adds a label and a text button to the builder.
     * Sets their positions and alignments.
     * Closes the screen when the button is clicked.
     *
     * @param builder The GuiBuilder object to build the GUI layout.
     */
    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(TextObject.translation("ultracraft.screen.dev.message"))
                .alignment(Alignment.LEFT)
                .position(() -> new Position(40, 40)));

        builder.add(TextButton.of(TextObject.translation("ultracraft.screen.dev.close"))
                .bounds(() -> new Bounds(client.getScaledWidth() / 2 - 50, client.getScaledHeight() - 40, 100, 20))
                .callback(caller -> this.close()));
    }

    /**
     * Renders the widget for the DevScreen.
     * Calls the superclass method to render the background.
     *
     * @param renderer  The Renderer object to render the widget.
     * @param mouseX    The x-coordinate of the mouse.
     * @param mouseY    The y-coordinate of the mouse.
     * @param deltaTime The time passed since the last frame.
     */
    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        renderBackground(renderer);

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }
}
