package com.ultreon.craft.client.gui.widget.properties;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public interface AlignmentProperty {
    @NotNull Alignment getAlignment();

    Widget<?> alignment(@NotNull Alignment alignment);
}
