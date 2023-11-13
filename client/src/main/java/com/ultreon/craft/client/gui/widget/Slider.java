package com.ultreon.craft.client.gui.widget;

import com.google.common.base.Preconditions;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.CallbackComponent;
import com.ultreon.craft.client.gui.widget.components.RangedValueComponent;
import com.ultreon.craft.client.gui.widget.components.TextComponent;
import com.ultreon.libs.commons.v0.Identifier;
import org.checkerframework.common.value.qual.IntRange;

import static com.ultreon.craft.client.UltracraftClient.id;

public class Slider extends Widget {
    private static final Identifier TEXTURE = id("textures/gui/slider.png");
    private final CallbackComponent<Slider> callback;
    private final RangedValueComponent value;
    private final TextComponent text;
    private boolean isHolding;
    private int holdStart;
    private int originalValue;

    public Slider(int value, int min, int max) {
        this(200, value, min, max);
    }

    public Slider(@IntRange(from = 21) int width, int value, int min, int max) {
        super(width, 21);

        this.ignoreBounds = true;

        Preconditions.checkArgument(max > min, "Max should be higher than min");

        this.callback = this.register(id("callback"), new CallbackComponent<>(it -> {
        }));
        this.value = this.register(id("value"), new RangedValueComponent(value, min, max));
        this.text = this.register(id("text"), new TextComponent());
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        int thumbX = this.pos.x + (this.size.width - 21) * (this.value.get() - this.value.min()) / (this.value.max() - this.value.min()) + 1;

        // Track
        renderer.blit(Slider.TEXTURE, this.pos.x, this.pos.y, 7, 21, 0, 0, 7, 21);
        renderer.blit(Slider.TEXTURE, this.pos.x + 7, this.pos.y, this.size.width - 14, 21, 7, 0, 7, 21);
        renderer.blit(Slider.TEXTURE, this.pos.x + this.size.width - 7, this.pos.y, 7, 21, 14, 0, 7, 21);

        // Thumb
        renderer.blit(Slider.TEXTURE, thumbX, this.pos.y, 18, 15, 21, 0, 18, 15);

        // Text
        renderer.drawTextCenter(this.text.get().copy().append(": ").append(this.value.get()), this.pos.x + this.size.width / 2, this.pos.y + 5, true);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        int thumbX = this.pos.x + (this.size.width - 21) * (this.value.get() - this.value.min()) / (this.value.max() - this.value.min()) + 1;

        if (Widget.isPosWithin(mouseX, mouseY, thumbX + 1, this.pos.y, 18, 15)) {
            this.originalValue = this.value.get();
            this.holdStart = mouseX;
            this.isHolding = true;
        }

        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        this.isHolding = false;

        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public void mouseMove(int mouseX, int mouseY) {
        super.mouseMove(mouseX, mouseY);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        if (this.isHolding) {
            this.value.set(this.originalValue + (this.value.max() - this.value.min()) * (mouseX - this.holdStart) / (this.size.width - 21));
            this.callback.call(this);
            return true;
        }

        return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
    }

    public CallbackComponent<Slider> callback() {
        return this.callback;
    }

    public RangedValueComponent value() {
        return this.value;
    }

    public TextComponent text() {
        return this.text;
    }
}