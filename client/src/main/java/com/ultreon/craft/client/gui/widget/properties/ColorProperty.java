package com.ultreon.craft.client.gui.widget.properties;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.client.util.Color;
import org.jetbrains.annotations.Nullable;

public interface ColorProperty {
    @Nullable Color getColor();

    @CanIgnoreReturnValue
    Widget<?> color(@Nullable Color color);
}
