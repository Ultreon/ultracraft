package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Config;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.craft.render.gui.GuiComponent;

public class FloatEntry extends ConfigEntry<Float> {
    private final float min;
    private final float max;

    public FloatEntry(String key, float value, float min, float max, Config config) {
        super(key, value, config);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Float read(String text) {
        return Float.parseFloat(text);
    }

    @Override
    public void setFromWidget(GuiComponent widget) {

    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }
}
