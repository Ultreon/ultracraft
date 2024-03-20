package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.ColorComponent;
import com.ultreon.craft.client.gui.widget.components.TextComponent;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Mth;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.ultreon.craft.client.UltracraftClient.id;

public class CycleButton<T> extends Button<CycleButton<T>> {
    private @Nullable TextObject label = null;
    private final TextComponent text;
    private final ColorComponent textColor;
    private T[] values;
    private Function<T, TextObject> formatter;
    private int cur;

    public CycleButton(@IntRange(from = 21) int width, @Nullable MutableText name) {
        super(width, 21);

        this.text = this.register(id("text"), new TextComponent(TextObject.empty()));
        this.textColor = this.register(id("text_color"), new ColorComponent(Color.WHITE));

        this.label = name;
        this.text.set(name);
    }

    public CycleButton(@IntRange(from = 21) int width, @IntRange(from = 21) int height, @Nullable MutableText name) {
        super(width, height);

        this.text = this.register(id("text"), new TextComponent(null));
        this.textColor = this.register(id("text_color"), new ColorComponent(Color.WHITE));

        this.label = name;
        this.text.set(name);
    }

    public CycleButton() {
        super(200, 21);

        this.text = this.register(id("text"), new TextComponent(TextObject.empty()));
        this.textColor = this.register(id("text_color"), new ColorComponent(Color.WHITE));
    }

    @Override
    public void revalidate() {
        TextObject lbl = this.label;
        if (lbl != null) {
            this.text().set(lbl.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        } else {
            this.text().set(this.formatter.apply(this.values[this.cur]));
        }

        super.revalidate();
    }

    @SafeVarargs
    public final CycleButton<T> values(T... values) {
        this.values = values;
        return this;
    }

    public final CycleButton<T> formatter(Function<T, TextObject> formatter) {
        this.formatter = formatter;
        return this;
    }

    @Override
    public CycleButton<T> position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public CycleButton<T> bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    public TextComponent text() {
        return this.text;
    }

    public ColorComponent textColor() {
        return this.textColor;
    }

    @Override
    public boolean click() {
        this.cur = (this.cur + 1) % this.values.length;
        if (this.label != null) {
            this.text().set(this.label.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        } else {
            this.text().set(this.formatter.apply(this.values[this.cur]));
        }
        this.callback.call(this);
        return true;
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        this.renderButton(renderer, mouseX, mouseY, texture, x, y);

        TextObject textObject = this.text.get();
        if (textObject != null) {
            renderer.textCenter(textObject, x + this.size.width / 2, y + (this.size.height / 2 - this.font.lineHeight + (this.isPressed() ? 2 : 0)), this.enabled ? this.textColor.get() : this.textColor.get().withAlpha(0x80));
        }
    }

    public T getValue() {
        return this.values[this.cur];
    }

    public int getIndex() {
        return this.cur;
    }

    public @Nullable TextObject getLabel() {
        return this.label;
    }

    public String getRawLabel() {
        return this.label != null ? this.label.getText() : this.formatter.apply(this.values[this.cur]).getText();
    }

    public CycleButton<T> index(int index) {
        this.cur = Mth.clamp(index, 0, this.values.length - 1);
        return this;
    }

    public CycleButton<T> value(T o) {
        this.cur = Mth.clamp(ArrayUtils.indexOf(this.values, o), 0, this.values.length - 1);
        return this;
    }

    public CycleButton<T> label(TextObject label) {
        this.label = label;
        return this;
    }

    public CycleButton<T> label(String label) {
        this.label = TextObject.literal(label);
        return this;
    }

    public CycleButton<T> labelTranslation(String label, Object... args) {
        this.label = TextObject.translation(label, args);
        return this;
    }
}
