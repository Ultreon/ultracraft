package com.ultreon.craft.client.gui.screens.container;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.menu.CrateMenu;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Identifier;

public class CrateScreen extends ContainerScreen {
    private static final int CONTAINER_SIZE = 40;
    private static final Identifier BACKGROUND = UltracraftClient.id("textures/gui/container/crate.png");
    private final CrateMenu menu;

    public CrateScreen(CrateMenu menu, TextObject title) {
        super(menu, title, CrateScreen.CONTAINER_SIZE);
        this.menu = menu;
    }

    @Override
    public int backgroundWidth() {
        return 181;
    }

    @Override
    public int backgroundHeight() {
        return 186;
    }

    @Override
    public Identifier getBackground() {
        return CrateScreen.BACKGROUND;
    }

    public CrateMenu getMenu() {
        return this.menu;
    }
}
