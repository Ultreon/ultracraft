package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.util.Color;

public class Label extends GuiComponent {
    private String text;
    private Alignment alignment;
    private Color color;

    public Label(int x, int y, String text, Alignment alignment) {
        this(x, y, text, alignment, Color.WHITE);
    }

    public Label(int x, int y, String text, Color color) {
        this(x, y, text, Alignment.LEFT, color);
    }

    public Label(int x, int y, String text) {
        this(x, y, text, Alignment.LEFT, Color.WHITE);
    }

    public Label(int x, int y, String text, Alignment alignment, Color color) {
        super(x, y, 0, 0);
        this.text = text;
        this.alignment = alignment;
        this.color = color;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Alignment getAlignment() {
        return this.alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        this.width = 0;
        this.height = 0;

        switch (this.alignment) {
            case LEFT:
                renderer.drawTextLeft(this.text, this.x, this.y, Color.WHITE);
                break;

            case CENTER:
                renderer.drawTextCenter(this.text, this.x, this.y, Color.WHITE);
                break;

            case RIGHT:
                renderer.drawTextRight(this.text, this.x, this.y, Color.WHITE);
                break;
        }
        renderer.drawTextLeft(this.text, this.x, this.y, this.color);
    }

    public enum Alignment {
        LEFT, CENTER, RIGHT

    }
}
