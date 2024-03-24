package com.ultreon.craft.client.config.entries;

import com.ultreon.craft.client.config.Configuration;
import com.ultreon.craft.client.config.gui.ConfigEntry;

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
