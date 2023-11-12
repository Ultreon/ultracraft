package com.ultreon.craft.client.gui;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.widget.StaticWidget;
import com.ultreon.craft.client.util.Resizer;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

import static com.ultreon.craft.client.UltracraftClient.TO_ZOOM;

public class LoadingOverlay implements StaticWidget {
    private final Resizer resizer;
    private final Texture ultreonLogoTex;
    private float progress;
    private final List<String> messages = new ArrayList<>();
    private final UltracraftClient client = UltracraftClient.get();
    private final Texture background;

    public LoadingOverlay(Texture background) {
        this.background = background;
        this.ultreonLogoTex = new Texture("assets/ultracraft/logo.png");

        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        int width = this.client.getScaledWidth();
        int height = this.client.getScaledHeight();

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
