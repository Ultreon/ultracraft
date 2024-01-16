package com.ultreon.craft.client.font;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Mth;

public class TextObjectRenderer {
    private final GlyphLayout layout = new GlyphLayout();
    private final TextObject text;
    private final Font font;
    private BitmapFont originalFont;
    private BitmapFont currentFont;
    private StringBuilder partBuilder = new StringBuilder();
    private float currentX;

    public TextObjectRenderer(TextObject text) {
        this.text = text;
        this.font = UltracraftClient.get().font;
        this.originalFont = this.font.bitmapFont;
        this.currentFont = this.font.bitmapFont;
    }

    public String getText() {
        return this.text.getText();
    }

    public void render(Renderer renderer, Color altColor, float x, float y, boolean shadow) {
        this.currentX = x;

        for (TextObject cur : this.text) {
            String rawText = cur.createString();
            Color color = Color.WHITE;
            boolean bold = false;
            boolean italic = false;
            boolean underlined = false;
            boolean strikethrough = false;
            boolean small = false;

            if (cur instanceof MutableText mutableText) {
                color = mutableText.getColor();
                bold = mutableText.isBold();
                italic = mutableText.isItalic();
                underlined = mutableText.isUnderlined();
                strikethrough = mutableText.isStrikethrough();
                small = mutableText.isSmall();
            }

            if (color == null) {
                color = altColor;
            }

            this.renderSingle(renderer, y, shadow, rawText, color, bold, italic, underlined, strikethrough, small);
        }
    }

    private void renderSingle(Renderer renderer, float y, boolean shadow, String rawText, Color color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean small) {
        float scale = 1;
        for (char c : rawText.toCharArray()) {
            if (!this.currentFont.getData().hasGlyph(c) || this.font.isForcingUnicode()) {
                this.currentFont = UltracraftClient.get().unifont == null ? this.originalFont : UltracraftClient.get().unifont;
                scale = 0.5F;
            } else {
                this.currentFont = this.font.bitmapFont;
                if (small) {
                    scale = Math.max(1f / UltracraftClient.get().getGuiScale(), 0.5F);
                }
            }

            this.partBuilder.append(c);

            if (this.currentFont != this.originalFont) {
                this.nextPart(renderer, y, shadow, color, bold, italic, underlined, strikethrough, scale);
            }
        }

        this.nextPart(renderer, y, shadow, color, bold, italic, underlined, strikethrough, scale);
    }

    private void nextPart(Renderer renderer, float y, boolean shadow, Color color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale) {
        var part = this.partBuilder.toString();
        this.renderPart(renderer, y, shadow, color, bold, italic, underlined, strikethrough, part, scale);

        this.originalFont = this.currentFont;

        this.layout.setText(this.currentFont, part);
        float width = this.font.width(part);
        this.currentX += width * scale;

        this.partBuilder = new StringBuilder();
    }

    private void renderPart(Renderer renderer, float y, boolean shadow, Color color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, String part, float scale) {
        if (this.currentFont == UltracraftClient.get().unifont && this.currentFont != null)
            this.font.drawTextScaled(renderer, this.currentFont, renderer.getBatch(), part, this.currentX, y + (this.font.bitmapFont.getLineHeight() - UltracraftClient.get().unifont.getLineHeight() * 0.5F) / 2, bold, italic, underlined, strikethrough, scale, color, shadow);
        else
            this.font.drawTextScaled(renderer, this.currentFont, renderer.getBatch(), part, this.currentX, y, bold, italic, underlined, strikethrough, scale, color, shadow);
    }
}
