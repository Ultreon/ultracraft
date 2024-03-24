package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.icon.Icon;
import com.ultreon.craft.util.Color;

import java.util.function.Supplier;

import static com.ultreon.craft.client.UltracraftClient.id;

public class IconButton extends Button<IconButton> {

    private final Icon icon;

    protected IconButton(Icon icon) {
        super(22, 26);
        this.icon = icon;
    }

    public static IconButton of(Icon icon) {
        return new IconButton(icon);
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        this.renderButton(renderer, mouseX, mouseY, texture, x, y);

        if (this.isPressed()) y += 2;
        renderer.blitColor(Color.WHITE.darker().darker());
        this.icon.render(renderer, x + 3, y + 3, 16, 16, deltaTime);
        renderer.blitColor(Color.WHITE);
        this.icon.render(renderer, x + 3, y + 2, 16, 16, deltaTime);
    }

    @Override
    public IconButton position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public IconButton bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    @Override
    public IconButton callback(Callback<IconButton> callback) {
        this.callback.set(callback);
        return this;
    }
}
