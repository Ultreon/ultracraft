package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Configuration;
import com.ultreon.craft.config.gui.ConfigEntry;

import java.util.UUID;

public class UUIDEntry extends ConfigEntry<UUID> {
    public UUIDEntry(String key, UUID value, Configuration config) {
        super(key, value, config);
    }

    @Override
    protected UUID read(String text) {
        return UUID.fromString(text);
    }

}
