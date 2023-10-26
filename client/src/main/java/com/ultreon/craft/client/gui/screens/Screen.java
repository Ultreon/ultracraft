package com.ultreon.craft.client.gui.screens;

import static com.badlogic.gdx.math.MathUtils.ceil;

import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.GuiContainer;

import javax.annotation.Nullable;
import java.util.List;

public class Screen extends GuiContainer {
    @Nullable private Screen back;
    protected final String title;

    public Screen(String title) {
        this(UltracraftClient.get().screen, title);
    }

    public Screen(@Nullable Screen back, String title) {
        super(0, 0, ceil(UltracraftClient.get().getWidth() / UltracraftClient.get().getGuiScale()), ceil(UltracraftClient.get().getHeight() / UltracraftClient.get().getGuiScale()));
        this.back = back;
        this.title = title;
    }

    public void tick() {
        super.tick();
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        this.clearWidgets();
        this.init();
    }

    public void init() {

    }

    public boolean close(@Nullable Screen to) {
        return true;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        this.renderBackground(renderer);

        super.render(renderer, mouseX, mouseY, deltaTime);
    }

    protected void renderBackground(Renderer renderer) {
        if (this.client.world != null) this.renderTransparentBackground(renderer);
        else this.renderSolidBackground(renderer);
    }

    protected void renderSolidBackground(Renderer renderer) {
        GuiComponent.fill(renderer, 0, 0, this.width, this.height, 0xff202020);
    }

    protected void renderTransparentBackground(Renderer renderer) {
        GuiComponent.fill(renderer, 0, 0, this.width, this.height, 0xa0202020);
    }

    public String getTitle() {
        return title;
    }

    public boolean canClose() {
        return true;
    }

    public boolean back() {
        if (!this.canClose()) return false;
        this.client.showScreen(this.back);
        return true;
    }

    public void filesDropped(List<FileHandle> files) {

    }
}
