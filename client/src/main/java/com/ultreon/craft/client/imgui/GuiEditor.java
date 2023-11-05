package com.ultreon.craft.client.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.client.gui.widget.UIContainer;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.client.gui.widget.properties.*;
import com.ultreon.craft.text.TextObject;
import imgui.ImGui;

public class GuiEditor {
    public void render(UltracraftClient client) {
        var currentScreen = client.screen;

        ImGuiEx.text("Classname:", () -> currentScreen == null ? null : currentScreen.getClass().getSimpleName());
        if (currentScreen != null) {
            var widgets = currentScreen.getWidgetsAt((int) (Gdx.input.getX() / client.getGuiScale()), (int) (Gdx.input.getY() / client.getGuiScale()));
            for (var widget : widgets) {
                if (widget != null) {
                    client.shapes.getBatch().begin();
                    if (widget instanceof UIContainer<?>) {
                        client.shapes.setColor(Color.CYAN);
                    } else {
                        client.shapes.setColor(Color.MAGENTA);
                    }
                    client.shapes.rectangle(
                            widget.getX() * client.getGuiScale(), widget.getY() * client.getGuiScale() - 1,
                            widget.getWidth() * client.getGuiScale() + 1, widget.getHeight() * client.getGuiScale() + 1);
                    client.shapes.getBatch().end();
                }
            }
            ImGuiEx.text("Widget:", () -> widgets.stream().findFirst().map(widget -> widget.path().getFileName()).orElse(null));
        }

        if (currentScreen != null) {
            GuiEditor.renderTools(currentScreen);
        }
    }

    private static void renderTools(Screen screen) {
        ImGuiEx.editBool("Enabled", "::enabled", screen::isEnabled, screen::enabled);
        ImGuiEx.editBool("Visible", "::visible", screen::isVisible, screen::visible);
        ImGuiEx.editString("Title", "::title", screen::getRawTitle, screen::title);
        ImGuiEx.button("Back", "::back", screen::back);

        if (ImGui.collapsingHeader("Widgets")) {
            ImGui.treePush();

            var children = screen.children();
            for (int i = 0, childrenSize = children.size; i < childrenSize; i++) {
                var component = children.get(i);
                if (component == null) continue;

                GuiEditor.renderWidgetTools(i, component);
            }

            ImGui.treePop();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderWidgetTools(int index, Widget<?> component) {
        if (ImGui.collapsingHeader("Widget #" + index + ": " + component.path().getFileName())) {
            ImGui.treePush();
            var id = component.path().toString();

            ImGuiEx.text("Package: ", () -> component.getClass().getPackageName());
            ImGuiEx.text("Classname: ", () -> component.getClass().getSimpleName());
            ImGuiEx.editBool("Enabled: ", id + "::enabled", component::isEnabled, component::enabled);
            ImGuiEx.editBool("Visible: ", id + "::visible", component::isVisible, component::visible);

            // Properties
            if (component instanceof BackgroundColorProperty property)
                ImGuiEx.editColor3("Background Color: ", id + "::backgroundColor", property::getBackgroundColor, property::backgroundColor);
            if (component instanceof ColorProperty property)
                ImGuiEx.editColor3("Color: ", id + "::color", property::getColor, property::color);
            if (component instanceof TextColorProperty property)
                ImGuiEx.editColor3("Text Color: ", id + "::textColor", property::getTextColor, property::textColor);
            if (component instanceof TextProperty<?> property)
                ImGuiEx.editString("Text: ", id + "::text", property::getRawText, property::text);
            if (component instanceof AlignmentProperty property)
                ImGuiEx.editEnum("Alignment: ", id + "::alignment", property::getAlignment, property::alignment);

            // Widgets
            if (component instanceof TextEntry entry)
                ImGuiEx.editString("Hint: ", id + "::hint", () -> entry.getHint().getText(), hint -> entry.hint(TextObject.nullToEmpty(hint)));
            if (component instanceof CycleButton button)
                ImGuiEx.editEnum("Value: ", id + "::value", button::getValue, button::value);

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editInt("X: ", id + "::pos::x", component::getX, component::x);
                ImGuiEx.editInt("Y: ", id + "::pos::y", component::getY, component::y);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Size")) {
                ImGui.treePush();
                ImGuiEx.editInt("Width: ", id + "::size::width", component::getWidth, component::width);
                ImGuiEx.editInt("Height: ", id + "::size::height", component::getHeight, component::height);
                ImGui.treePop();
            }

            if (component instanceof UIContainer container && ImGui.collapsingHeader("Children")) {
                ImGui.treePush();
                Array<Widget<?>> children = container.children();
                for (int i = 0, childrenSize = children.size; i < childrenSize; i++) {
                    var child = children.get(i);
                    if (child == null) continue;

                    GuiEditor.renderWidgetTools(i, child);
                }
                ImGui.treePop();
            }

            ImGui.treePop();
        }
    }
}
