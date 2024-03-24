package com.ultreon.craft.client.config.entries;

import com.ultreon.craft.client.config.Configuration;
import com.ultreon.craft.client.config.gui.ConfigEntry;

public class LongEntry extends ConfigEntry<Long> {
    private final long min;
    private final long max;

    public LongEntry(String key, long value, long min, long max, Configuration config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Long read(String text) {
        return Long.parseLong(text);
    }

    public long getMin() {
        return this.min;
    }

    public long getMax() {
        return this.max;
    }

}
