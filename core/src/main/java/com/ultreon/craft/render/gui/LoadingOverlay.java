package com.ultreon.craft.render.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.ultreon.craft.Resizer;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Drawable;
import com.ultreon.craft.render.Renderer;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

import static com.ultreon.craft.UltreonCraft.TO_ZOOM;
import static com.ultreon.craft.UltreonCraft.id;

public class LoadingOverlay implements Drawable {
    private final Resizer resizer;
    private final Texture ultreonLogoTex;
    private float progress;
    private final List<String> messages = new ArrayList<>();
    private final BitmapFont font = new BitmapFont();
    private final UltreonCraft game = UltreonCraft.get();
    private final Texture background;

    public LoadingOverlay(Texture background) {
        this.background = background;
        this.ultreonLogoTex = new Texture("assets/craft/logo.png");

        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        int width = this.game.getScaledWidth();
        int height = this.game.getScaledHeight();

        renderer.blit(this.background, 0, 0, width, height, 0, 0, this.background.getWidth(), this.background.getHeight(), this.background.getWidth(), this.background.getHeight());
        Vec2f thumbnail = this.resizer.thumbnail(width * TO_ZOOM, height * TO_ZOOM);

        float drawWidth = thumbnail.x;
        float drawHeight = thumbnail.y;

        float drawX = (width - drawWidth) / 2;
        float drawY = (height - drawHeight) / 2;
        renderer.blit(this.ultreonLogoTex, (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, 1920, 1080, 1920, 1080);

        renderer.fill(200, height - height / 3, width - 400, 8, Color.argb(0x7fffffff));
        renderer.fill(200, height - height / 3, (int) ((width - 400) * this.progress), 8, Color.rgb(0xffffff));
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
