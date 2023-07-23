package com.ultreon.craft.render.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Drawable;
import com.ultreon.craft.render.Renderer;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

import static com.ultreon.craft.UltreonCraft.id;

public class LoadingOverlay implements Drawable {
    private float progress;
    private final List<String> messages = new ArrayList<>();
    private final BitmapFont font = new BitmapFont();
    private final UltreonCraft game = UltreonCraft.get();
    private final Texture background;

    public LoadingOverlay(Texture background) {
        this.background = background;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        int width = this.game.getScaledWidth();
        int height = this.game.getScaledHeight();

        renderer.texture(this.background, 0, 0, width, height, 0, 0, this.background.getWidth(), this.background.getHeight(), this.background.getWidth(), this.background.getHeight());
        renderer.fill(200, height / 3, width - 400, 8, Color.argb(0x7fffffff));
        renderer.fill(200, height / 3, (int) ((width - 400) * this.progress), 8, Color.rgb(0xffffff));
    }

    public void setProgress(@Range(from = 0, to = 1) float progress) {
        this.progress = progress;
    }

    public void log(String message) {
        if (this.messages.size() == 3) this.messages.remove(2);
        this.messages.add(0, message);
    }

    public float getProgress() {
        return this.progress;
    }
}
