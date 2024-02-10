package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.TextInputScreen;
import com.ultreon.craft.client.util.Renderable;
import com.ultreon.craft.util.Color;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class VirtualKeyboard implements Renderable {
    private final TextInputScreen screen;

    public VirtualKeyboard() {
        this.screen = new TextInputScreen(this);
        this.screen.init(UltracraftClient.get().getScaledWidth(), UltracraftClient.get().getScaledHeight());
    }

    public void open(VirtualKeyboardEditCallback callback, VirtualKeyboardSubmitCallback submitCallback) {
        this.screen.setSubmitCallback(submitCallback);
        this.screen.setEditCallback(callback);
        this.screen.resize(UltracraftClient.get().getScaledWidth(), UltracraftClient.get().getScaledHeight());
    }

    public void close() {
        this.screen.setSubmitCallback(() -> {});
        this.screen.setEditCallback(input -> {});
        UltracraftClient.get().gamepadInput.handleVirtualKeyboardClosed(this.screen.getInput());
    }

    @Override
    public void render(@NotNull Renderer guiGraphics, int mouseX, int mouseY, float deltaTime) {
        guiGraphics.pushMatrix();
        guiGraphics.translate(0, 0, 1000);
        guiGraphics.fill(0, 0, UltracraftClient.get().getScaledWidth(), UltracraftClient.get().getScaledHeight(), Color.argb(0x80000000));
        this.screen.render(guiGraphics, mouseX, mouseY, deltaTime);
        guiGraphics.popMatrix();
    }

    public TextInputScreen getScreen() {
        return screen;
    }
}
