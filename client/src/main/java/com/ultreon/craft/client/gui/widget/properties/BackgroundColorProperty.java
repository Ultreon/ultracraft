package com.ultreon.craft.client.gui.widget.properties;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.util.Color;

public interface BackgroundColorProperty extends BackgroundColorProvider {
    @CanIgnoreReturnValue
    Widget<?> backgroundColor(Color backgroundColor);
}
