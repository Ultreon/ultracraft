package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.properties.CallbackProperty;
import com.ultreon.craft.client.gui.widget.properties.TextProperty;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.text.TextObject;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;

import static com.ultreon.craft.client.UltracraftClient.id;

@SuppressWarnings("unchecked")
public class TextEntry<T extends TextEntry<T>> extends Widget<T> implements TextProperty<T>, CallbackProperty<T> {
    private String text = "";
    private TextObject hint = TextObject.EMPTY;
    private CharPredicate charPredicate = c -> true;

    private int cursorIdx = 0;
    private float cursorX;
    private Callback<T> callback = caller -> {
    };

    /**
     * @param x      the X position to create the text entry at
     * @param y      the Y position to create the text entry at
     * @param width  the width of the text entry.
     * @param height the height of the text entry.
     */
    public TextEntry(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height, T... typeGetter) {
        super(x, y, width, height, typeGetter);
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        final int u = this.enabled ? this.focused ? 12 : 0 : 24;
        final int v = 0;
        final int tx = this.pos.x;
        final int ty = this.pos.y - 2;
        final int tw = this.size.width;
        final int th = this.size.height + 3;

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

        renderer.drawTextLeft(this.text, this.pos.x + 5, this.pos.y + 6, false);
        if (this.text.isEmpty()) {
            renderer.drawTextLeft(this.hint, this.pos.x + 5, this.pos.y + 6, Color.WHITE.withAlpha(0x80), false);
        }

        if (this.focused) {
            renderer.drawLine(this.pos.x + 3 + this.cursorX, this.pos.y + 5, this.pos.x + 3 + this.cursorX, this.pos.y + this.size.height - 6, Color.WHITE);
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

    @Override
    public void revalidate() {
        this.cursorX = this.font.width(this.text.substring(0, this.cursorIdx));

        this.callback.call((T) this);
    }

    @Override
    public String getName() {
        return "TextEntry";
    }

    public TextObject getText() {
        return TextObject.nullToEmpty(this.text);
    }

    @Override
    public T text(TextObject text) {
        this.text = text.getText();
        return (T) this;
    }

    @Override
    public String getRawText() {
        return this.text;
    }

    public TextObject getHint() {
        return this.hint;
    }

    public T hint(TextObject hint) {
        this.hint = hint;
        return (T) this;
    }

    @ApiStatus.Internal
    public CharPredicate getCharPredicate() {
        return this.charPredicate;
    }

    public void setCharPredicate(CharPredicate charPredicate) {
        this.charPredicate = charPredicate;
    }

    @ApiStatus.Internal
    public Callback<T> getCallback() {
        return this.callback;
    }

    @Override
    public T callback(Callback<T> callback) {
        this.callback = callback;
        return (T) this;
    }

    public int getCursorIdx() {
        return this.cursorIdx;
    }

    public void setCursorIdx(int cursorIdx) {
        this.cursorIdx = cursorIdx;
    }
}
