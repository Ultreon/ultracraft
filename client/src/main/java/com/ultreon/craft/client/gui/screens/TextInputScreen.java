package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.KeyMappingIcon;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.KeyboardButton;
import com.ultreon.craft.client.input.gamepad.VirtualKeyboard;
import com.ultreon.craft.client.input.gamepad.VirtualKeyboardEditCallback;
import com.ultreon.craft.client.input.gamepad.VirtualKeyboardSubmitCallback;
import com.ultreon.craft.client.input.keyboard.KeyboardLayout;
import com.ultreon.craft.text.TextObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TextInputScreen extends Screen {
    private final KeyboardLayout layout;
    private final VirtualKeyboard virtualKeyboard;
    private String input;
    private boolean shift;
    private boolean caps;
    private VirtualKeyboardSubmitCallback submitCallback = () -> {};
    private VirtualKeyboardEditCallback editCallback = s -> {};
    private final List<KeyboardButton> buttons = new ArrayList<KeyboardButton>();

    public TextInputScreen(VirtualKeyboard virtualKeyboard) {
        super(TextObject.literal("Text Input"));
        this.virtualKeyboard = virtualKeyboard;

        this.font = UltracraftClient.get().font;
        this.layout = UltracraftClient.get().gamepadInput.getLayout();
    }

    public void setSubmitCallback(VirtualKeyboardSubmitCallback callback) {
        this.submitCallback = callback;
    }

    public void setEditCallback(VirtualKeyboardEditCallback callback) {
        this.editCallback = callback;
    }

    @Override
    public void build(GuiBuilder builder) {
        this.setInput(UltracraftClient.get().gamepadInput.getVirtualKeyboardValue());

        char[][] layoutLayout = layout.getLayout(shift || caps);
        for (int rowIdx = 0, layoutLayoutLength = layoutLayout.length; rowIdx < layoutLayoutLength; rowIdx++) {
            char[] row = layoutLayout[rowIdx];

            int x = 0;
            for (char symbol : row) {
                KeyMappingIcon icon = KeyMappingIcon.byChar(symbol);
                if (icon == null) continue;

                this.addButton(builder, symbol, x, row.length * 16, rowIdx, icon);

                x += icon.width;
            }
        }

        this.revalidate();
    }

    @Override
    public void renderBackground(@NotNull Renderer renderer) {

    }

    private void addButton(GuiBuilder builder, char c, int dx, int rowLength, int rowIdx, KeyMappingIcon icon) {
        builder.add(new KeyboardButton(icon, button -> {
            if (c >= 0x20) {
                setInput(getInput() + c);
                return;

            }
            switch (c) {
                case '\n', '\r' -> this.submit();
                case '\b' -> this.backspace();
                case '\t' -> setInput(getInput() + "    ");
                case '\0', '\1', '\3', '\4', '\5', '\6', '\7' -> {
                    // TODO: Add support for other controller input characters
                }
            }
        }).position(() -> {
            int keyboardWidth = rowLength;
            if (rowIdx == 0) keyboardWidth += 16;
            if (rowIdx == 1) keyboardWidth += 7;
            if (rowIdx == 2) keyboardWidth += 27;
            if (rowIdx == 3) keyboardWidth += 33;
            if (rowIdx == 4) keyboardWidth += 41;

            int x = this.size.width / 2 - keyboardWidth / 2;

            return new Position(x + dx, rowIdx * 16 + getHeight() - 85 - getYOffset());
        }));
    }

    private int getYOffset() {
        if (this.client != null) {
            return this.client.screen instanceof ChatScreen ? 32 : 0;
        }

        return 0;
    }

    private void submit() {
        virtualKeyboard.close();
        submitCallback.onSubmit();
    }

    private void backspace() {
        if (!getInput().isEmpty()) {
            setInput(getInput().substring(0, getInput().length() - 1));
        }
    }

    @Override
    public boolean onClose(Screen next) {
        this.virtualKeyboard.close();

        this.submitCallback = () -> {};

        return super.onClose(next);
    }

    public String getInput() {
        return input;
    }

    private void setInput(String input) {
        this.input = input;
        this.editCallback.onInput(input);
    }
}
