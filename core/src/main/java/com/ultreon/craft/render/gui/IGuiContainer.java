package com.ultreon.craft.render.gui;

import com.ultreon.craft.render.Renderer;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface IGuiContainer {

    void renderChildren(Renderer renderer, int mouseX, int mouseY, float deltaTime);

    List<? extends GuiComponent> children();
}
