package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Config;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.craft.render.gui.GuiComponent;

public class IntEntry extends ConfigEntry<Integer> {
    private final int min;
    private final int max;

    public IntEntry(String key, int value, int min, int max, Config config) {
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

    @Override
    public void setFromWidget(GuiComponent widget) {

    }
}
