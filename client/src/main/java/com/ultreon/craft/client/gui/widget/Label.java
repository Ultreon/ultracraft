package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.AlignmentComponent;
import com.ultreon.craft.client.gui.widget.components.ColorComponent;
import com.ultreon.craft.client.gui.widget.components.ScaleComponent;
import com.ultreon.craft.client.gui.widget.components.TextComponent;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.jetbrains.annotations.ApiStatus;

import static com.ultreon.craft.client.UltracraftClient.id;

@ApiStatus.NonExtendable
public class Label extends Widget {
    private final AlignmentComponent alignment;
    private final ColorComponent textColor;
    private final TextComponent text;
    private final ScaleComponent scale;

    public Label() {
        this(Alignment.LEFT, Color.WHITE);
    }

    public Label(Color textColor) {
        this(Alignment.LEFT, textColor);
    }

    public Label(Alignment alignment) {
        this(alignment, Color.WHITE);
    }

    public Label(Alignment alignment, Color textColor) {
        super(0, 0);
        this.alignment = this.register(id("alignment"), new AlignmentComponent(alignment));
        this.textColor = this.register(id("text_color"), new ColorComponent(textColor));
        this.text = this.register(id("text"), new TextComponent(TextObject.empty()));
        this.scale = this.register(id("scale"), new ScaleComponent(1));
    }

    @Override
    public void renderBackground(Renderer renderer, float deltaTime) {
        this.size.idt();
        int scale = this.scale.get();
        var text = this.text.get();
        var textColor = this.textColor.get();

        switch (this.alignment.get()) {
            case LEFT:
                renderer.drawTextScaledLeft(text, scale, this.pos.x, this.pos.y, textColor);
                break;

            case CENTER:
                renderer.drawTextScaledCenter(text, scale, this.pos.x, this.pos.y, textColor);
                break;

            case RIGHT:
                renderer.drawTextScaledRight(text, scale, this.pos.x, this.pos.y, textColor);
                break;
        }
    }

    @Override
    public String getName() {
        return "Label";
    }

    public ScaleComponent scale() {
        return this.scale;
    }

    public TextComponent text() {
        return this.text;
    }

    public AlignmentComponent alignment() {
        return this.alignment;
    }
}
