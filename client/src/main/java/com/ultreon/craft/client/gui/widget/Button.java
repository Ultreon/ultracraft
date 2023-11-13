package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.CallbackComponent;
import com.ultreon.craft.client.gui.widget.components.ColorComponent;
import com.ultreon.craft.client.gui.widget.components.TextComponent;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;

import static com.ultreon.craft.client.UltracraftClient.id;

public class Button extends Widget {
    private final CallbackComponent<Button> callback;
    private boolean pressed;
    private final TextComponent text;
    private final ColorComponent textColor;

    public Button() {
        this(200);
    }

    /**
     * @param width the width of the button
     */
    public Button(@IntRange(from = 21) int width) {
        this(width, 21);
    }

    /**
     * @param width  the width of the button
     * @param height the height of the button
     */
    public Button(@IntRange(from = 21) int width, @IntRange(from = 21) int height) {
        super(width, height);

        this.text = this.register(id("text"), new TextComponent(TextObject.empty()));
        this.textColor = this.register(id("text_color"), new ColorComponent(Color.WHITE));
        this.callback = this.register(id("callback"), new CallbackComponent<>(it -> {
        }));
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        int u;
        if (this.enabled) u = this.isWithinBounds(mouseX, mouseY) ? 21 : 0;
        else u = 42;
        int v = this.isPressed() ? 21 : 0;

        renderer.setTextureColor(Color.WHITE);
        renderer.blit(texture, x, y, 7, 7, u, v, 7, 7);
        renderer.blit(texture, x + 7, y, this.size.width - 14, 7, 7 + u, v, 7, 7);
        renderer.blit(texture, x + this.size.width - 7, y, 7, 7, 14 + u, v, 7, 7);
        renderer.blit(texture, x, y + 7, 7, this.size.height - 14, u, 7 + v, 7, 7);
        renderer.blit(texture, x + 7, y + 7, this.size.width - 14, this.size.height - 14, 7 + u, 7 + v, 7, 7);
        renderer.blit(texture, x + this.size.width - 7, y + 7, 7, this.size.height - 14, 14 + u, 7 + v, 7, 7);
        renderer.blit(texture, x, y + this.size.height - 7, 7, 7, u, 14 + v, 7, 7);
        renderer.blit(texture, x + 7, y + this.size.height - 7, this.size.width - 14, 7, 7 + u, 14 + v, 7, 7);
        renderer.blit(texture, x + this.size.width - 7, y + this.size.height - 7, 7, 7, 14 + u, 14 + v, 7, 7);

        renderer.drawTextCenter(this.text.get(), x + this.size.width / 2, y + (this.size.height / 2 - this.font.lineHeight + (this.isPressed() ? 2 : 0)), this.enabled ? this.textColor.get() : this.textColor.get().withAlpha(0x80));
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        return !this.click();
    }

    @ApiStatus.OverrideOnly
    public boolean click() {
        if (!this.enabled) return false;

        CallbackComponent<Button> callback = this.callback;
        if (callback == null) {
            return true;
        }
        callback.call(this);
        return false;
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        if (!this.enabled) return false;

        this.pressed = true;
        return super.mousePress(x, y, button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        this.pressed = false;
        return super.mouseRelease(x, y, button);
    }

    public boolean isPressed() {
        return this.pressed && this.enabled;
    }

    @Override
    public String getName() {
        return "Button";
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    public TextComponent text() {
        return this.text;
    }

    public ColorComponent textColor() {
        return this.textColor;
    }

    public CallbackComponent<Button> callback() {
        return this.callback;
    }
}
