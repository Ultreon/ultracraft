package com.ultreon.craft.client.gui.screens.tabs;

import com.ultreon.craft.client.gui.widget.ScrollableContainer;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class TabContent extends ScrollableContainer {
    private final TabbedUI parent;
    private TextObject title;

    public TabContent(TabbedUI parent, int x, int y, int width, int height, TextObject title) {
        super(x, y, width, height);
        this.backgroundColor(Color.TRANSPARENT);
        this.parent = parent;
        this.title = title;
    }

    @Override
    public void revalidate() {
        super.revalidate();

        int curHeight = 0;
        for (Widget widget : this.getWidgets()) {
            curHeight = widget.getX() + widget.getHeight();
        }

        this.contentHeight = curHeight;
        this.contentWidth = size.width;
    }

    public TabbedUI parent() {
        return parent;
    }

    public TextObject title() {
        return this.title;
    }

    public TabContent title(TextObject title) {
        this.title = title;
        return this;
    }
}
