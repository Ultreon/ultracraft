package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.Input;
import com.ultreon.craft.client.gui.screens.ChatScreen;

public class ChatTextEntry extends TextEntry {
    private final ChatScreen screen;

    public ChatTextEntry(ChatScreen screen) {
        this.screen = screen;
        this.screen.focused = this;
        this.focused = true;
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.ENTER) {
            this.screen.send();
            return true;
        }

        return super.keyPress(keyCode);
    }

    @Override
    public void onFocusLost() {
        this.root.focused = this;
    }

    public ChatScreen getScreen() {
        return this.screen;
    }
}
