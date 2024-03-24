package com.ultreon.craft.client.gui.widget.layout;

import com.ultreon.craft.client.gui.widget.UIContainer;
import com.ultreon.craft.client.gui.widget.Widget;

public class StandardLayout implements Layout {
    @Override
    public void relayout(UIContainer<?> container) {
        for (Widget widget : container.getWidgets()) {
            widget.setPos(widget.getPreferredPos());
            widget.setSize(widget.getPreferredSize());
        }
    }
}
