package com.ultreon.craft.client.config.entries;

import com.ultreon.craft.client.config.Configuration;
import com.ultreon.craft.client.config.gui.ConfigEntry;

public class BooleanEntry extends ConfigEntry<Boolean> {
    public BooleanEntry(String key, boolean value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected Boolean read(String text) {
        return Boolean.parseBoolean(text);
    }

}
