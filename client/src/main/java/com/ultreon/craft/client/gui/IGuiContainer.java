package com.ultreon.craft.client.gui;

import java.util.List;

public interface IGuiContainer {

    void renderChildren(Renderer renderer, int mouseX, int mouseY, float deltaTime);

    List<? extends GuiComponent> children();
}
