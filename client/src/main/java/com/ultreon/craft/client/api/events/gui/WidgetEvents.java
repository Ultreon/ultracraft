package com.ultreon.craft.client.api.events.gui;

import com.ultreon.craft.client.gui.widget.UIContainer;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.events.api.Event;

public class WidgetEvents {
    public static final Event<WidgetAdded> WIDGET_ADDED = Event.withValue();
    public static final Event<WidgetRemoved> WIDGET_REMOVED = Event.withValue();

    @FunctionalInterface
    public interface WidgetAdded {
        void onWidgetAdded(UIContainer<?> screen, Widget widget);
    }

    @FunctionalInterface
    public interface WidgetRemoved {
        void onWidgetRemoved(UIContainer<?> screen, Widget widget);
    }
}
