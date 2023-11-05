package com.ultreon.craft.client.gui.widget.properties;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.widget.Widget;
import org.jetbrains.annotations.ApiStatus;

public interface CallbackProperty<T extends Widget<T>> {
    Callback<T> getCallback();

    @CanIgnoreReturnValue
    @ApiStatus.OverrideOnly
    T callback(Callback<T> callback);

    default void callback(T widget) {
        this.getCallback().call(widget);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    @ApiStatus.NonExtendable
    default void _callback(Object widget) {
        this.getCallback().call((T) widget); // Generics ftw
    }
}
