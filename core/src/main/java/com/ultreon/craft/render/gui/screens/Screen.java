package com.ultreon.craft.render.gui.screens;

import static com.badlogic.gdx.math.MathUtils.ceil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.GuiContainer;

import javax.annotation.Nullable;
import java.util.List;

public class Screen extends GuiContainer {
    @Nullable private Screen back;
    protected final String title;

    public Screen(String title) {
        this(UltreonCraft.get().currentScreen, title);
    }

    public Screen(@Nullable Screen back, String title) {
        super(0, 0, ceil(UltreonCraft.get().getWidth() / UltreonCraft.get().getGuiScale()), ceil(UltreonCraft.get().getHeight() / UltreonCraft.get().getGuiScale()));
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
        this.show();
    }

    public void show() {

    }

    public void hide() {

    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderBackground(renderer);

        super.render(renderer, mouseX, mouseY, deltaTime);
    }

    protected void renderBackground(Renderer renderer) {
        if (game.world != null) {
            fill(renderer, 0, 0, width, height, 0xa0202020);
        } else {
            fill(renderer, 0, 0, width, height, 0xff202020);
        }
    }

    public String getTitle() {
        return title;
    }

    public boolean canClose() {
        return true;
    }

    public boolean back() {
        if (!this.canClose()) return false;
        this.game.showScreen(this.back);
        return true;
    }

    public void filesDropped(List<FileHandle> files) {

    }
}
