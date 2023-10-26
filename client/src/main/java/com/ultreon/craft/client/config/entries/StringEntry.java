package com.ultreon.craft.client.config.entries;

import com.ultreon.craft.client.config.Configuration;
import com.ultreon.craft.client.config.gui.ConfigEntry;

public class StringEntry extends ConfigEntry<String> {
    public StringEntry(String key, String value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected String read(String text) {
        return text;
    }

}
