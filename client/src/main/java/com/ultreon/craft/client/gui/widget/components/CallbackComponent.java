package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.component.GameComponent;
import com.ultreon.libs.commons.v0.Identifier;

public class CallbackComponent<T extends Widget<T>> extends UIComponent {
    private Callback<T> callback;

    public CallbackComponent(Identifier id, Callback<T> callback) {
        super(id);
        this.callback = callback;
    }

    public Callback<T> getCallback() {
        return callback;
    }

    public void setCallback(Callback<T> callback) {
        this.callback = callback;
    }
}
