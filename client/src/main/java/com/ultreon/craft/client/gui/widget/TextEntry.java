package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.util.Color;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;

import static com.ultreon.craft.client.UltracraftClient.id;

public class TextEntry extends GuiComponent {
    private String text = "";
    private String hint;
    private CharPredicate charPredicate = c -> true;

    private int cursorIdx = 0;
    private float cursorX;
    private Callback<TextEntry> callback = caller -> {
    };

    /**
     * @param x      the X position to create the text entry at
     * @param y      the Y position to create the text entry at
     * @param width  the width of the text entry.
     * @param height the height of the text entry.
     */
    public TextEntry(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        this(x, y, width, height, "");
    }

    /**
     * @param x      the X position to create the text entry at
     * @param y      the Y position to create the text entry at
     * @param width  the width of the text entry.
     * @param height the height of the text entry.
     * @param hint   the text shown when there's no text in the entry.
     */
    public TextEntry(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height, String hint) {
        super(x, y, width, height);
        this.hint = hint;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        final int u = this.enabled ? this.focused ? 12 : 0 : 24;
        final int v = 0;
        final int tx = this.x - 1;
        final int ty = this.y - 4;
        final int tw = this.width + 2;
        final int th = this.height + 5;

        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/text_entry.png"));
        renderer.setTextureColor(Color.WHITE);
        renderer.blit(texture, tx, ty, 4, 4, u, v, 4, 4, 36, 12);
        renderer.blit(texture, tx + 4, ty, tw - 8, 4, 4 + u, v, 4, 4, 36, 12);
        renderer.blit(texture, tx + tw - 4, ty, 4, 4, 8 + u, v, 4, 4, 36, 12);
        renderer.blit(texture, tx, ty + 4, 4, th - 8, u, 4 + v, 4, 4, 36, 12);
        renderer.blit(texture, tx + 4, ty + 4, tw - 8, th - 8, 4 + u, 4 + v, 4, 4, 36, 12);
        renderer.blit(texture, tx + tw - 4, ty + 4, 4, th - 8, 8 + u, 4 + v, 4, 4, 36, 12);
        renderer.blit(texture, tx, ty + th - 4, 4, 4, u, 8 + v, 4, 4, 36, 12);
        renderer.blit(texture, tx + 4, ty + th - 4, tw - 8, 4, 4 + u, 8 + v, 4, 4, 36, 12);
        renderer.blit(texture, tx + tw - 4, ty + th - 4, 4, 4, 8 + u, 8 + v, 4, 4, 36, 12);

        renderer.drawText(this.text, this.x + 3, this.y + 6, false, this.width - 6, "...");
        if (this.text.isEmpty()) {
            renderer.drawText(this.hint, this.x + 3, this.y + 6, Color.WHITE.withAlpha(0x80), false, this.width - 6, "...");
        }

        if (this.focused) {
            renderer.drawLine(this.x + 3 + this.cursorX, this.y + 5, this.x + 3 + this.cursorX, this.y + this.height - 6, Color.WHITE);
        }
    }

    @Override
    public boolean charType(char character) {
        if (!Character.isISOControl(character) && this.charPredicate.test(character)) {
            this.text += character;
            this.cursorIdx++;
            this.revalidate();
            return true;
        }
        return super.charType(character);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.BACKSPACE && this.cursorIdx > 0) {
            var start = this.text.substring(0, this.cursorIdx - 1);
            var end = this.text.substring(this.cursorIdx);
            this.text = start + end;
            this.cursorIdx--;
            this.revalidate();
        }
        if (keyCode == Input.Keys.FORWARD_DEL && this.cursorIdx < this.text.length()) {
            var start = this.text.substring(0, this.cursorIdx);
            var end = this.text.substring(this.cursorIdx + 1);
            this.text = start + end;
            this.revalidate();
        }

        if (keyCode == Input.Keys.LEFT && this.cursorIdx > 0) {
            this.cursorIdx--;
            this.revalidate();
        }
        if (keyCode == Input.Keys.RIGHT && this.cursorIdx < this.text.length()) {
            this.cursorIdx++;
            this.revalidate();
        }

        return super.keyPress(keyCode);
    }

    private void revalidate() {
        this.cursorX = this.font.width(this.text.substring(0, this.cursorIdx));

        this.callback.call(this);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHint() {
        return this.hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @ApiStatus.Internal
    public CharPredicate getCharPredicate() {
        return this.charPredicate;
    }

    public void setCharPredicate(CharPredicate charPredicate) {
        this.charPredicate = charPredicate;
    }

    @ApiStatus.Internal
    public Callback<TextEntry> getCallback() {
        return this.callback;
    }

    public void setCallback(Callback<TextEntry> callback) {
        this.callback = callback;
    }

    public int getCursorIdx() {
        return this.cursorIdx;
    }
}
