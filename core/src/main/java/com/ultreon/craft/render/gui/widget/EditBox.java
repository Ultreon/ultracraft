package com.ultreon.craft.render.gui.widget;

import com.badlogic.gdx.Input;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.GuiComponent;
import com.ultreon.craft.text.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditBox extends GuiComponent {
    @Nullable
    private String hint;
    private String text = "";
    private int maxLength = 128;
    private int cursor;
    private boolean error;
    private Validator validator = query -> true;

    public EditBox(int x, int y, int width, @Nullable String hint) {
        super(x, y, width, 21);
        this.hint = hint;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.fill(this.getX(), this.getY(), this.getWidth(), this.getHeight(), Color.BLACK);
        renderer.box(this.getX()+2, this.getY()+2, this.getWidth()-4, this.getHeight()-4, this.getBorderColor());

        renderer.pushScissors(this.getX() + 3, this.getY() + 3, this.getWidth() - 6, this.getHeight() - 6);
        final int offset = 7;
        if (this.text.isEmpty() && this.hint != null) {
            renderer.drawText(this.hint, this.getX() + offset, this.getY() + this.getHeight() - offset, this.getBorderColor().withAlpha(128));
        } else if (!this.text.isEmpty()) {
            renderer.drawText(this.text, this.getX() + offset, this.getY() + this.getHeight() - offset, this.getBorderColor());
        }
        renderer.popScissors();
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @NotNull
    protected Color getBorderColor() {
        if (this.error) return Color.rgb(0xff404040);
        if (this.isFocused()) return Color.WHITE;
        return Color.GRAY;
    }

    public @Nullable String getHint() {
        return this.hint;
    }

    public void setHint(@Nullable String hint) {
        this.hint = hint;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
        this.revalidate();
    }

    private void revalidate() {
        this.error = !this.validator.validate(this.text);
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean charType(char character) {
        if (this.text.length() >= this.maxLength) {
            return false;
        }

        String first = this.text.substring(0, this.cursor);
        String last = this.text.substring(this.cursor);
        this.text = first + character + last;
        this.cursor++;
        this.revalidate();

        return true;
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.BACKSPACE && !this.text.isEmpty() && this.cursor > 0) {
            String first = this.text.substring(0, this.cursor - 1);
            String last = this.text.substring(this.cursor);
            this.cursor--;
            this.text = first + last;
            this.revalidate();
        }
        if (keyCode == Input.Keys.FORWARD_DEL && this.cursor < this.text.length() - 1) {
            String first = this.text.substring(0, this.cursor);
            String last = this.text.substring(this.cursor + 1);
            this.text = first + last;
            this.revalidate();
        }

        if (keyCode == Input.Keys.RIGHT && this.cursor < this.text.length()) this.cursor++;
        if (keyCode == Input.Keys.LEFT && this.cursor > 0) this.cursor--;

        return super.keyPress(keyCode);
    }

    public boolean isError() {
        return this.error;
    }

    public int getCursor() {
        return this.cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }
}
