package com.ultreon.craft.client.imgui;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.GuiContainer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.TextEntry;
import imgui.ImGui;

public class GuiEditor {
    public void render(UltracraftClient client) {
        var currentScreen = client.screen;

        ImGuiEx.text("Classname:", () -> currentScreen == null ? null : currentScreen.getClass().getSimpleName());
        if (currentScreen != null) {
            var exactWidgetAt = currentScreen.getExactWidgetAt((int) (Gdx.input.getX() / client.getGuiScale()), (int) (Gdx.input.getY() / client.getGuiScale()));
            if (exactWidgetAt != null) {
                client.shapes.getBatch().begin();
                client.shapes.setColor(1.0F, 0.0F, 1.0F, 1.0F);
                client.shapes.rectangle(
                        exactWidgetAt.getX() * client.getGuiScale(), exactWidgetAt.getY() * client.getGuiScale(),
                        exactWidgetAt.getWidth() * client.getGuiScale(), exactWidgetAt.getHeight() * client.getGuiScale()
                );
                client.shapes.getBatch().end();
            }
            ImGuiEx.text("Widget:", () -> exactWidgetAt == null ? null : exactWidgetAt.getClass().getSimpleName());
        }

        if (currentScreen != null) {
            GuiEditor.renderTools(currentScreen);
        }
    }

    private static void renderTools(Screen screen) {
        ImGuiEx.editBool("Enabled", "::enabled", () -> screen.enabled, v -> screen.enabled = v);
        ImGuiEx.editBool("Visible", "::visible", () -> screen.visible, v -> screen.visible = v);
        ImGuiEx.editString("Title", "::title", screen::getTitle, screen::setTitle);
        ImGuiEx.button("Back", "::back", screen::back);

        if (ImGui.collapsingHeader("Widgets")) {
            ImGui.treePush();

            var children = screen.children();
            for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
                var component = children.get(i);
                if (component == null) continue;

                GuiEditor.renderWidgetTools(i, component);
            }

            ImGui.treePop();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderWidgetTools(int index, GuiComponent component) {
        if (ImGui.collapsingHeader("Widget #" + index + ": " + component.getClass().getSimpleName())) {
            ImGui.treePush();
            var id = component.path().toString();

            ImGuiEx.text("Package: ", () -> component.getClass().getPackageName());
            ImGuiEx.text("Classname: ", () -> component.getClass().getSimpleName());
            ImGuiEx.editBool("Enabled: ", id + "::enabled", () -> component.enabled, v -> component.enabled = v);
            ImGuiEx.editBool("Visible: ", id + "::visible", () -> component.visible, v -> component.visible = v);
            ImGuiEx.editColor4("Color", id + "::color", component::getBackgroundColor, component::setBackgroundColor);

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editInt("X: ", id + "::pos::x", component::getX, component::setX);
                ImGuiEx.editInt("Y: ", id + "::pos::y", component::getY, component::setY);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Size")) {
                ImGui.treePush();
                ImGuiEx.editInt("Width: ", id + "::size::width", component::getWidth, component::setWidth);
                ImGuiEx.editInt("Height: ", id + "::size::height", component::getHeight, component::setHeight);
                ImGui.treePop();
            }

            if (component instanceof GuiContainer container) {
                if (ImGui.collapsingHeader("Children")) {
                    ImGui.treePush();
                    var children = container.children();
                    for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
                        var child = children.get(i);
                        if (child == null) continue;

                        GuiEditor.renderWidgetTools(i, child);
                    }
                    ImGui.treePop();
                }
            }
            if (component instanceof Button button) {
                if (ImGui.collapsingHeader("Button")) {
                    ImGui.treePush();
                    ImGuiEx.editString("Text: ", id + "::text", button::getMessage, button::setMessage);
                    ImGuiEx.editColor3("Text Color: ", id + "::textColor", button::getTextColor, button::setTextColor);
                    ImGui.treePop();
                }
            }
            if (component instanceof TextEntry entry) {
                if (ImGui.collapsingHeader("Text Entry")) {
                    ImGui.treePush();
                    ImGuiEx.editString("Text: ", id + "::text", entry::getText, entry::setText);
                    ImGuiEx.editString("Hint: ", id + "::hint", entry::getHint, entry::setHint);
                    ImGui.treePop();
                }
            }
            if (component instanceof CycleButton button) {
                if (ImGui.collapsingHeader("Cycle Button")) {
                    ImGui.treePush();
                    ImGuiEx.editString("Text: ", id + "::text", button::getMessage, button::setMessage);
                    ImGuiEx.editColor3("Text Color: ", id + "::textColor", button::getTextColor, button::setTextColor);
                    ImGuiEx.editEnum("Value: ", id + "::value", button::getValue, button::setValue);
                    ImGui.treePop();
                }
            }
            ImGui.treePop();
        }
    }
}
