package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Configuration;
import com.ultreon.craft.config.gui.ConfigEntry;

public class StringEntry extends ConfigEntry<String> {
    public StringEntry(String key, String value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected String read(String text) {
        return text;
    }

}
