package com.ultreon.craft.client.font;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.client.Constants;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class Font implements Disposable {
    @SuppressWarnings("GDXJavaStaticResource")
    static final BitmapFont UNIFONT = UltracraftClient.get().unifont;
    final BitmapFont bitmapFont;
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
        UltracraftClient.get().deferDispose(this);
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
                currentX += glyph.xadvance * scale;
            }
        }
    }

    public void drawText(Renderer renderer, TextObject text, float x, float y, Color color, boolean shadow) {
        TextObjectRenderer textRenderer = new TextObjectRenderer(text);
        textRenderer.render(renderer, color, x, y, shadow);
    }

    boolean isForcingUnicode() {
        return this.client.forceUnicode && !this.isSpecial();
    }

    public boolean isSpecial() {
        return this.special;
    }

    void drawTextScaled(Renderer renderer, BitmapFont font, Batch batch, String text, float x, float y, float scale, Color color, boolean shadow) {
        this.drawTextScaled(renderer, font, batch, text, x, y, false, false, false, false, scale, color, shadow);
    }

    void drawTextScaled(Renderer renderer, BitmapFont font, Batch batch, String text, float x, float y, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale, Color color, boolean shadow) {
        renderer.pushMatrix();
        renderer.scale(scale, scale);
        if (shadow) {
            float shadowX = x;
            if (Constants.SHADOW_OFFSET) shadowX += 1;
            this.draw(renderer, font, color.darker().darker(), batch, text, shadowX, y / scale + 1, bold, italic, underlined, strikethrough, scale);
        }

        this.draw(renderer, font, color, batch, text, x, y / scale, bold, italic, underlined, strikethrough, scale);
        renderer.popMatrix();
    }

    private void draw(Renderer renderer, BitmapFont font, Color color, Batch batch, String text, float x, float y, boolean bold, boolean italic, boolean underlined, boolean strikethrough, float scale) {
        font.setUseIntegerPositions(true);
        font.setColor(color.toGdx());
        font.draw(batch, text, x / scale, y);

        if (bold)
            this.draw(renderer, font, color, batch, text, x + 1, y, false, italic, underlined, strikethrough, scale);

        if (underlined)
            renderer.line(x, (int) (y + (font.getLineHeight() + 2)) - 0.5f, x + (this.width(text)), (int) (y + (font.getLineHeight() + 2)) - 0.5f, color);

        if (strikethrough)
            renderer.line(x, (int) (y + (font.getLineHeight()) / 2), x + (this.width(text)), (int) (y + (font.getLineHeight()) / 2), color);
    }

    public float width(String text) {
        if (text.isEmpty()) {
            return 0;
        }

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
                width += glyph.xadvance * scale;
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

    public int width(TextObject text) {
        int width = 0;

        for (TextObject child : text) {
            boolean isBold = false;
            if (child instanceof MutableText mutableText) {
                isBold = mutableText.isBold();
            }
            this.layout.setText(this.bitmapFont, child.createString());
            width += (int) (this.layout.width + (isBold ? 1 : 0));
        }
        return width;
    }

    public void dispose() {
        this.bitmapFont.dispose();
    }
}
