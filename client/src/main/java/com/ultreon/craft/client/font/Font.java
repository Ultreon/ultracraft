package com.ultreon.craft.client.font;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.text.TextObject;

public class Font {
    @SuppressWarnings("GDXJavaStaticResource")
    private static final BitmapFont UNIFONT = UltracraftClient.get().unifont;
    private final BitmapFont bitmapFont;
    private final UltracraftClient client = UltracraftClient.get();
    public final int lineHeight;
    private final boolean special;
    private final GlyphLayout layout = new GlyphLayout();

    public Font(BitmapFont bitmapFont) {
        this(bitmapFont, false);
    }

    public Font(BitmapFont bitmapFont, boolean special) {
        this.bitmapFont = bitmapFont;
        this.lineHeight = MathUtils.ceil(bitmapFont.getLineHeight());
        this.special = special;
    }

    public void drawText(Renderer renderer, String text, float x, float y, Color color, boolean shadow) {
        float currentX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            BitmapFont currentFont = this.bitmapFont;
            float scale = 1;
            if (!currentFont.getData().hasGlyph(c) || this.isForcingUnicode()) {
                currentFont = Font.UNIFONT;
                scale = 0.5F;
            }
            if (currentFont == Font.UNIFONT) {
                this.drawTextScaled(renderer, currentFont, renderer.getBatch(), String.valueOf(c), currentX, y + (this.bitmapFont.getLineHeight() - Font.UNIFONT.getLineHeight() * 0.5F) / 2, scale, color, shadow);
            } else {
                this.drawTextScaled(renderer, currentFont, renderer.getBatch(), String.valueOf(c), currentX, y, scale, color, shadow);
            }
            this.layout.setText(currentFont, String.valueOf(c));
            BitmapFont.Glyph glyph = currentFont.getData().getGlyph(c);
            if (glyph != null) {
                currentX += (float)glyph.xadvance * scale;
            }
        }
    }

    private boolean isForcingUnicode() {
        return this.client.forceUnicode && !this.isSpecial();
    }

    public boolean isSpecial() {
        return this.special;
    }

    private void drawTextScaled(Renderer renderer, BitmapFont font, Batch batch, String text, float x, float y, float scale, Color color, boolean shadow) {
        renderer.pushMatrix();
        renderer.scale(scale, scale);
        if (shadow) {
            font.setColor(color.darker().darker().toGdx());
            font.draw(batch, text, x / scale, y / scale + 1);
        }
        font.setColor(color.toGdx());
        font.draw(batch, text, x / scale, y / scale);
        renderer.popMatrix();
    }

    public float width(String text) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            BitmapFont currentFont = this.bitmapFont;
            float scale = 1;
            if (!currentFont.getData().hasGlyph(c) || this.isForcingUnicode()) {
                currentFont = Font.UNIFONT;
                scale = 0.5F;
            }
            this.layout.setText(currentFont, String.valueOf(c));

            BitmapFont.Glyph glyph = currentFont.getData().getGlyph(c);
            if (glyph != null) {
                width += (float)glyph.xadvance * scale;
            }
        }
        return width - 1;
    }

    public void setColor(float r, float g, float b, float a) {
        this.bitmapFont.setColor(r, g, b, a);
        Font.UNIFONT.setColor(r, g, b, a);
    }

    public void setColor(Color color) {
        com.badlogic.gdx.graphics.Color gdx = color.toGdx();
        this.bitmapFont.setColor(gdx);
        Font.UNIFONT.setColor(gdx);
    }

    public float width(TextObject text) {
        return this.width(text.getText());
    }

    public void dispose() {
        this.bitmapFont.dispose();
    }
}
