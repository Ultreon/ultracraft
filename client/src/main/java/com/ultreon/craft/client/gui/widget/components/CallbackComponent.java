package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.ApiStatus;

public class CallbackComponent<T extends Widget> extends UIComponent {
    private Callback<T> callback;

    public CallbackComponent(Callback<T> callback) {
        super();
        this.callback = callback;
    }

    public void set(Callback<T> callback) {
        this.callback = callback;
    }

    public void call(T caller) {
        this.callback.call(caller);
    }

    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public void call0(Object caller) {
        this.callback.call((T) caller);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleImGui(String path, ElementID key, Widget widget) {

    }
}
