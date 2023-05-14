package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.GuiContainer;

import static com.badlogic.gdx.math.MathUtils.ceil;

public class Screen extends GuiContainer {
    protected final String title;
    protected final GlyphLayout titleLayout;

    public Screen(String title) {
        super(0, 0, ceil(UltreonCraft.get().getWidth() / UltreonCraft.get().getGuiScale()), ceil(UltreonCraft.get().getHeight() / UltreonCraft.get().getGuiScale()));
        this.title = title;
        this.titleLayout = new GlyphLayout(font, title);
    }

    public void tick() {
        super.tick();
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
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

    public boolean canCloseOnEsc() {
        return true;
    }
}
