package com.ultreon.craft.client;

import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.mozilla.javascript.RhinoException;

import java.util.List;

public class RhinoExceptionScreen extends Screen {
    private final List<RhinoException> rhinoExceptions;
    private SelectionList<RhinoException> list;

    public RhinoExceptionScreen(List<RhinoException> rhinoExceptions) {
        super(TextObject.translation("ultracraft.screen.xeox_errors"));

        this.rhinoExceptions = rhinoExceptions;
    }

    private void renderError(Renderer renderer, RhinoException value, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        renderer.textCenter(value.getMessage(), this.list.getX() + this.list.getWidth() / 2, y + 5, Color.WHITE);
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(title.copy().style(style -> style.color(Color.rgb(0xff4040)))).position(() -> new Position(this.size.width / 2, 10)).alignment(Alignment.CENTER).scale(2));

        this.list = builder.add(new SelectionList<RhinoException>())
                .bounds(() -> new Bounds(0, 40, this.size.width, this.size.height - 40))
                .entries(this.rhinoExceptions)
                .itemHeight(20)
                .selectable(false)
                .itemRenderer(this::renderError);
    }
}
