package com.ultreon.craft.client.gui.widget.properties;

import com.ultreon.craft.client.gui.widget.Widget;

public interface NumberValueProperty<T extends Widget<T>> {
    double getValue();
    T value(double v);
}
