package com.ultreon.craft.client.gui.widget.properties;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.util.Color;
import org.jetbrains.annotations.NotNull;

public interface TextColorProperty {
    @NotNull Color getTextColor();

    @CanIgnoreReturnValue
    TextColorProperty textColor(@NotNull Color color);
}
