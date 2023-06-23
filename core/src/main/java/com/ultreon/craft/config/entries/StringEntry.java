package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Config;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.craft.render.gui.GuiComponent;

public class StringEntry extends ConfigEntry<String> {
    public StringEntry(String key, String value, Config config) {
        super(key, value, config);
    }

    @Override
    protected String read(String text) {
        return text;
    }

    @Override
    public void setFromWidget(GuiComponent widget) {

    }
}
