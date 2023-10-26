package com.ultreon.craft.client.config.entries;

import com.ultreon.craft.client.config.Configuration;
import com.ultreon.craft.client.config.gui.ConfigEntry;

public class IntEntry extends ConfigEntry<Integer> {
    private final int min;
    private final int max;

    public IntEntry(String key, int value, int min, int max, Configuration config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Integer read(String text) {
        return Integer.parseInt(text);
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

}
