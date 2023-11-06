package com.ultreon.craft.client.gui.screens.container;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.text.TextObject;
import com.ultreon.libs.commons.v0.Identifier;

public class InventoryScreen extends ContainerScreen {
    private static final int CONTAINER_SIZE = 40;
    private static final Identifier BACKGROUND = UltracraftClient.id("textures/gui/container/inventory.png");
    private final Inventory inventory;

    public InventoryScreen(Inventory inventory, TextObject title) {
        super(inventory, title, InventoryScreen.CONTAINER_SIZE);
        this.inventory = inventory;
    }

    @Override
    public int backgroundWidth() {
        return 181;
    }

    @Override
    public int backgroundHeight() {
        return 110;
    }

    @Override
    public Identifier getBackground() {
        return InventoryScreen.BACKGROUND;
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}
