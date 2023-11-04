package com.ultreon.craft.text;

import com.ultreon.libs.commons.v0.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public enum FontWeight {
    ULTRA_BOLD(2.75f), EXTRA_BOLD(2.5f), HEAVY(2.25f), BOLD(2.0f), DEMI_BOLD(1.75f), MEDIUM(1.5f), SEMI_BOLD(1.25f), REGULAR(1.0f), DEMI_LIGHT(0.875f), LIGHT(0.75f), EXTRA_LIGHT(0.5f), THIN(100);

    private final float weight;

    FontWeight(float weight) {
        this.weight = weight;
    }

    public float getWeight() {
        return this.weight;
    }

    @NotNull
    public static FontWeight closestTo(float weight) {
        var lastDiff = Float.MAX_VALUE;
        FontWeight cur = null;
        for (var value : FontWeight.values()) {
            if (Mth.diff(weight, value.weight) < lastDiff) {
                cur = value;
            }
        }

        assert cur != null;
        return cur;
    }

    @NotNull
    public static FontWeight closestTo(float weight, Collection<FontWeight> values) {
        var lastDiff = Float.MAX_VALUE;
        FontWeight cur = null;
        for (var value : values) {
            if (Mth.diff(weight, value.weight) < lastDiff) {
                cur = value;
            }
        }

        assert cur != null;
        return cur;
    }
}
