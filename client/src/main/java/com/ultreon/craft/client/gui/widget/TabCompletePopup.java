package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.util.Renderable;
import com.ultreon.craft.text.ChatColor;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Mth;

import java.util.Arrays;

public class TabCompletePopup implements Renderable {
    public int x;
    public int y;
    public boolean visible;

    String[] values = new String[0];
    private int index;
    private final Font font = UltracraftClient.get().font;
    private int width;
    private int height;

    public TabCompletePopup(int x, int y) {
        this.y = y;
    }

    public void up() {
        this.index = Mth.clamp(this.index - 1, 0, this.values.length - 1);
    }

    public void down() {
        this.index = Mth.clamp(this.index + 1, 0, this.values.length - 1);
    }

    public String get() {
        return this.values[this.index];
    }

    public void setValues(String[] values) {
        this.values = values;
        this.index = 0;
        this.width = Arrays.stream(this.values).mapToInt(text -> (int) this.font.width(text)).max().orElse(0) + 4;
        this.height = Arrays.stream(this.values).mapToInt(text -> this.font.lineHeight + 4).sum();
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (!this.visible || this.values.length == 0) return;

        var textX = this.x + 2;
        var textY = this.y - this.height + 2;
        renderer.fill(this.x, this.y - this.height, this.width, this.height, Color.BLACK.withAlpha(0x80));
        String[] strings = this.values;
        for (int i = 0, stringsLength = strings.length; i < stringsLength; i++) {
            String value = strings[i];
            renderer.textLeft(value, textX, textY, i == this.index ? ChatColor.YELLOW : ChatColor.WHITE);
            textY += this.font.lineHeight + 4;
        }
    }
}
