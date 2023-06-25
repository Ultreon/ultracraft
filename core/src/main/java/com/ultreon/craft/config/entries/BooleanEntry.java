package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Configuration;
import com.ultreon.craft.config.gui.ConfigEntry;

public class BooleanEntry extends ConfigEntry<Boolean> {
    public BooleanEntry(String key, boolean value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected Boolean read(String text) {
        return Boolean.parseBoolean(text);
    }

}
