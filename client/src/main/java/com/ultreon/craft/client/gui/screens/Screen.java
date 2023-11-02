package com.ultreon.craft.client.gui.screens;

import static com.badlogic.gdx.math.MathUtils.ceil;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.GuiContainer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Screen extends GuiContainer {
    @Nullable private final Screen back;
    @NotNull protected final String title;

    /**
     * Constructs a new screen with the given title.
     *
     * @param title the title of the screen.
     */
    public Screen(String title) {
        this(UltracraftClient.get().screen, title);
    }

    /**
     * Constructs a new screen with the given back screen and title.
     *
     * @param back the back screen.
     * @param title the title of the screen.
     */
    public Screen(@Nullable Screen back, String title) {
        super(0, 0, ceil(UltracraftClient.get().getWidth() / UltracraftClient.get().getGuiScale()), ceil(UltracraftClient.get().getHeight() / UltracraftClient.get().getGuiScale()));
        this.back = back;
        this.title = title;
    }

    /**
     * Ticks the screen.
     */
    public void tick() {
        super.tick();
    }

    /**
     * Resizes the screen.
     *
     * @param width the new width of the screen.
     * @param height the new height of the screen.
     */
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        // Reinitialize the screen.
        this.clearWidgets();
        this.init();
    }

    /**
     * (Re)initializes the screen.
     */
    public void init() {

    }

    /**
     * Closes the screen.
     *
     * @param to the next screen to be displayed.
     * @return true to allow the screen to close, false to prevent the screen from closing.
     */
    public boolean close(@Nullable Screen to) {
        return true;
    }

    /**
     * Renders this screen.
     *
     * @param renderer renderer to draw/render with.
     * @param mouseX X-coordinate of mouse cursor.
     * @param mouseY Y-coordinate of mouse cursor.
     * @param deltaTime value of {@link Graphics#getDeltaTime()}
     */
    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        this.renderBackground(renderer);

        super.render(renderer, mouseX, mouseY, deltaTime);
    }

    /**
     * Renders the background of this screen.
     * By default renders a transparent background if the world is loaded, otherwise renders a solid background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderBackground(Renderer renderer) {
        if (this.client.world != null) this.renderTransparentBackground(renderer);
        else this.renderSolidBackground(renderer);
    }

    /**
     * Renders a solid background
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderSolidBackground(Renderer renderer) {
        GuiComponent.fill(renderer, 0, 0, this.width, this.height, 0xff202020);
    }

    /**
     * Renders a transparent background.
     *
     * @param renderer renderer to draw/render with.
     */
    protected void renderTransparentBackground(Renderer renderer) {
        GuiComponent.fill(renderer, 0, 0, this.width, this.height, 0xa0202020);
    }

    /**
     * @return the title of this screen.
     */
    public @NotNull String getTitle() {
        return this.title;
    }

    /**
     * @return true if this screen can be closed, false otherwise.
     */
    @ApiStatus.OverrideOnly
    public boolean canClose() {
        return true;
    }

    /**
     * @return true if this screen successfully closes, false otherwise.
     */
    public boolean back() {
        if (!this.canClose()) return false;
        this.client.showScreen(this.back);
        return true;
    }

    /**
     * Handles files being dropped into this screen.
     *
     * @param files list of files being dropped.
     */
    @ApiStatus.OverrideOnly
    public void filesDropped(List<FileHandle> files) {

    }
}
