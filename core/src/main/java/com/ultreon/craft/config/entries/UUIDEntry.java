package com.ultreon.craft.config.entries;

import com.ultreon.craft.config.Config;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.craft.render.gui.GuiComponent;

import java.util.UUID;

public class UUIDEntry extends ConfigEntry<UUID> {
    public UUIDEntry(String key, UUID value, Config config) {
        super(key, value, config);
    }

    @Override
    protected UUID read(String text) {
        return UUID.fromString(text);
    }

    @Override
    public void setFromWidget(GuiComponent widget) {

    }
}
