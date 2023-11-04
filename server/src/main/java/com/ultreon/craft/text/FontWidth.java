package com.ultreon.craft.text;

import com.ultreon.libs.commons.v0.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public enum FontWidth {
    EXTENDED(1.5f), SEMI_EXTENDED(1.25f), REGULAR(1.0f), SEMI_CONDENSED(0.875f), CONDENSED(0.75f);

    private final float width;

    FontWidth(float width) {
        this.width = width;
    }

    public float getWidth() {
        return this.width;
    }

    @NotNull
    public static FontWidth closestTo(float width) {
        var lastDiff = Float.MAX_VALUE;
        FontWidth cur = null;
        for (var value : FontWidth.values()) {
            if (Mth.diff(width, value.width) < lastDiff) {
                cur = value;
            }
        }

        assert cur != null;
        return cur;
    }

    @NotNull
    public static FontWidth closestTo(float width, Collection<FontWidth> values) {
        var lastDiff = Float.MAX_VALUE;
        FontWidth cur = null;
        for (var value : values) {
            if (Mth.diff(width, value.width) < lastDiff) {
                cur = value;
            }
        }

        assert cur != null;
        return cur;
    }
}
