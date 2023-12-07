package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.craft.network.packets.c2s.C2SRequestTabComplete;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.Input.Keys.*;

public class ChatTextEntry extends TextEntry {
    private final ChatScreen screen;
    private final TabCompletePopup popup = new TabCompletePopup(0, 0);
    private int completeX = -1;

    public ChatTextEntry(ChatScreen screen) {
        this.screen = screen;
        this.screen.focused = this;
        this.focused = true;
    }

    @Override
    public boolean keyPress(int keyCode) {
        switch (keyCode) {
            case ENTER -> {
                if (this.popup.visible && this.popup.values.length > 0) {
                    this.complete("");
                    this.popup.visible = false;
                } else {
                    this.screen.send();
                }
                return true;
            }
            case TAB -> {
                if (this.popup.visible && this.popup.values.length > 0) {
                    this.complete("");
                } else {
                    this.client.connection.send(new C2SRequestTabComplete(this.getValue()));
                    this.popup.visible = true;
                }
                return true;
            }
            case UP -> {
                if (this.popup.visible && this.popup.values.length > 0) {
                    this.popup.up();
                }
            }
            case DOWN -> {
                if (this.popup.visible && this.popup.values.length > 0) {
                    this.popup.down();
                }
            }
            case ESCAPE -> {
                if (this.popup.visible) {
                    this.popup.visible = false;
                    return true;
                }
            }
        }

        boolean b = super.keyPress(keyCode);

        if (this.getCursorIdx() < this.completeX) {
            this.popup.visible = false;
            this.popup.setValues(new String[0]);
        } else {
            this.completeX = this.getValue().lastIndexOf(' ') + 1;
        }
        return b;
    }

    private void complete(String s) {
        String value = this.revalidateCompleteX();
        if (value.startsWith("/")) {
            this.value("/" + value.substring(1, this.completeX) + this.popup.get() + s);
        } else {
            this.completeX = this.getCursorIdx();
            this.value(value.substring(0, this.completeX) + this.popup.get() + s);
        }
        this.setCursorIdx(this.getValue().length());
        this.revalidateCursor();
    }

    @Override
    public boolean charType(char character) {
        if (character == ' ' && this.popup.visible && this.popup.values.length > 0) {
            this.complete(" ");
            this.popup.visible = false;
            return true;
        }

        boolean b = super.charType(character);
        if (b) {
            String value = this.revalidateCompleteX();
            this.client.connection.send(new C2SRequestTabComplete(value));
        }
        return b;
    }

    @NotNull
    private String revalidateCompleteX() {
        String value = this.getValue();
        if (value.startsWith("/")) {
            if (value.contains(" ")) {
                this.completeX = value.lastIndexOf(' ') + 1;
            } else {
                this.completeX = 1;
            }
        } else {
            this.completeX = value.length();
        }
        return value;
    }

    @Override
    public void onFocusLost() {
        this.root.focused = this;
    }

    public ChatScreen getScreen() {
        return this.screen;
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        this.popup.y = this.getY();
        this.popup.render(renderer, mouseX, mouseY, deltaTime);
    }

    public void onTabComplete(String[] options) {
        String s = this.getValue().replaceAll(" .*^", "");
        this.completeX = s.length();
        this.popup.x = (int) this.font.width(this.getValue().substring(0, s.length()));
        this.popup.setValues(options);
    }
}
