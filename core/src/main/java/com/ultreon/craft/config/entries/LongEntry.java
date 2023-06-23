package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Config;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.craft.render.gui.GuiComponent;

public class LongEntry extends ConfigEntry<Long> {
    private final long min;
    private final long max;

    public LongEntry(String key, long value, long min, long max, Config config) {
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

    @Override
    public void setFromWidget(GuiComponent widget) {

    }
}
