package com.ultreon.craft.client.config.entries;

import com.ultreon.craft.client.config.Configuration;
import com.ultreon.craft.client.config.gui.ConfigEntry;

public class FloatEntry extends ConfigEntry<Float> {
    private final float min;
    private final float max;

    public FloatEntry(String key, float value, float min, float max, Configuration config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Float read(String text) {
        return Float.parseFloat(text);
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }
}
