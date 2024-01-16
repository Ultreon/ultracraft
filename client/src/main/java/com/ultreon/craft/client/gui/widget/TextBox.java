package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.CallbackComponent;
import com.ultreon.craft.client.gui.widget.components.TextComponent;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static com.ultreon.craft.client.UltracraftClient.id;

@SuppressWarnings("unchecked")
public class TextBox extends Widget {
    private CharPredicate filter = c -> true;

    private int cursorIdx = 0;
    private float cursorX;
    private String value = "";
    private TextComponent hint;
    private CallbackComponent<TextBox> callback;

    /**
     * @param width  the width of the text entry.
     * @param height the height of the text entry.
     */
    public TextBox(@IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);

        this.hint = this.register(id("hint"), new TextComponent());
        this.callback = this.register(id("callback"), new CallbackComponent<>(caller -> {
        }));
    }

    public static TextBox of(String value) {
        TextBox textBox = new TextBox();
        textBox.value = value;
        return textBox;
    }

    public static TextBox of() {
        return new TextBox();
    }

    @Override
    public TextBox position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public Widget bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    public TextBox() {
        this(200, 21);
    }

    public TextBox(int width) {
        this(width, 21);
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        final int u;
        if (this.enabled) {
            u = this.focused ? 12 : 0;
        } else {
            u = 24;
        }
        final int v = 0;
        final int tx = this.pos.x;
        final int ty = this.pos.y - 2;
        final int tw = this.size.width;
        final int th = this.size.height + 3;

        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/text_entry.png"));
        renderer.blitColor(Color.WHITE)
                .blit(texture, tx, ty, 4, 4, u, v, 4, 4, 36, 12)
                .blit(texture, tx + 4, ty, tw - 8, 4, 4 + u, v, 4, 4, 36, 12)
                .blit(texture, tx + tw - 4, ty, 4, 4, 8 + u, v, 4, 4, 36, 12)
                .blit(texture, tx, ty + 4, 4, th - 8, u, 4 + v, 4, 4, 36, 12)
                .blit(texture, tx + 4, ty + 4, tw - 8, th - 8, 4 + u, 4 + v, 4, 4, 36, 12)
                .blit(texture, tx + tw - 4, ty + 4, 4, th - 8, 8 + u, 4 + v, 4, 4, 36, 12)
                .blit(texture, tx, ty + th - 4, 4, 4, u, 8 + v, 4, 4, 36, 12)
                .blit(texture, tx + 4, ty + th - 4, tw - 8, 4, 4 + u, 8 + v, 4, 4, 36, 12)
                .blit(texture, tx + tw - 4, ty + th - 4, 4, 4, 8 + u, 8 + v, 4, 4, 36, 12);

        renderer.textLeft(this.value, this.pos.x + 5, this.pos.y + 6, false);
        if (this.value.isEmpty()) {
            renderer.textLeft(this.hint.get(), this.pos.x + 5, this.pos.y + 6, Color.WHITE.withAlpha(0x80), false);
        }

        if (this.focused) {
            renderer.line(this.pos.x + 3 + this.cursorX, this.pos.y + 5, this.pos.x + 3 + this.cursorX, this.pos.y + this.size.height - 6, Color.WHITE);
        }
    }

    @Override
    public void onFocusGained() {
        super.onFocusGained();

        Gdx.input.setOnscreenKeyboardVisible(true);
    }

    @Override
    public void onFocusLost() {
        super.onFocusLost();

        Gdx.input.setOnscreenKeyboardVisible(false);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        if (button == 0) {
            this.cursorIdx = this.getCursorIdx(mouseX, mouseY);
            this.revalidateCursor();
        }
        return super.mousePress(mouseX, mouseY, button);
    }

    private int getCursorIdx(int mouseX, int mouseY) {
        var relativeX = mouseX - this.pos.x;
        var relativeY = mouseY - this.pos.y;

        var value = this.value;
        var cursorIdx = 0;

        var x = 0;
        for (int i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            var charWidth = this.font.width(c);

            if (x + charWidth > relativeX) {
                cursorIdx = i;
                break;
            }

            x += charWidth;
            cursorIdx++;
        }

        return cursorIdx;

    }

    @Override
    public boolean charType(char character) {
        if (!Character.isISOControl(character) && this.filter.test(character)) {
            this.value += character;
            this.cursorIdx++;
            this.revalidateCursor();
            return true;
        }
        return super.charType(character);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.BACKSPACE && this.cursorIdx > 0) {
            var start = this.value.substring(0, this.cursorIdx - 1);
            var end = this.value.substring(this.cursorIdx);
            this.value = start + end;
            this.cursorIdx--;
            this.revalidateCursor();
        }
        if (keyCode == Input.Keys.FORWARD_DEL && this.cursorIdx < this.value.length()) {
            var start = this.value.substring(0, this.cursorIdx);
            var end = this.value.substring(this.cursorIdx + 1);
            this.value = start + end;
            this.revalidateCursor();
        }

        if (keyCode == Input.Keys.LEFT && this.cursorIdx > 0) {
            this.cursorIdx--;
            this.revalidateCursor();
        }
        if (keyCode == Input.Keys.RIGHT && this.cursorIdx < this.value.length()) {
            this.cursorIdx++;
            this.revalidateCursor();
        }

        return super.keyPress(keyCode);
    }

    public void revalidateCursor() {
        this.cursorX = this.font.width(this.value.substring(0, this.cursorIdx)) + 2;

        this.callback.call(this);
    }

    @Override
    public String getName() {
        return "TextEntry";
    }

    public String getValue() {
        return this.value;
    }

    public TextBox value(String value) {
        this.value = value;
        return this;
    }

    public TextBox hint(TextObject text) {
        this.hint.set(text);
        return this;
    }

    public TextBox filter(CharPredicate filter) {
        this.filter = filter;
        return this;
    }

    public TextBox callback(Callback<TextBox> callback) {
        this.callback.set(callback);
        return this;
    }

    public TextComponent hint() {
        return this.hint;
    }

    @ApiStatus.Internal
    public CharPredicate getFilter() {
        return this.filter;
    }

    public int getCursorIdx() {
        return this.cursorIdx;
    }

    public void setCursorIdx(int cursorIdx) {
        this.cursorIdx = cursorIdx;
    }

    public CallbackComponent<TextBox> callback() {
        return this.callback;
    }
}
