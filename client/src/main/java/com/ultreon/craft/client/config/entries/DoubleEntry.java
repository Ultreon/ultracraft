package com.ultreon.craft.client.config.entries;

import com.ultreon.craft.client.config.Configuration;
import com.ultreon.craft.client.config.gui.ConfigEntry;

public class DoubleEntry extends ConfigEntry<Double> {
    private final double min;
    private final double max;

    public DoubleEntry(String key, double value, double min, double max, Configuration config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Double read(String text) {
        return Double.parseDouble(text);
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

}
