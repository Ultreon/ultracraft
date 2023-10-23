package com.ultreon.craft.render.gui.screens.container;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.render.Renderer;
import com.ultreon.libs.commons.v0.Identifier;

public class InventoryScreen extends ContainerScreen {
    private static final int CONTAINER_SIZE = 40;
    private static final Identifier BACKGROUND = UltreonCraft.id("textures/gui/container/inventory.png");
    private final Inventory inventory;

    public InventoryScreen(Inventory inventory, String title) {
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
