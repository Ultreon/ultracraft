package com.ultreon.xeox.loader;

import com.ultreon.craft.ModOrigin;

import java.util.Collection;
import java.util.Collections;

public record XeoxMetadata(String id, String name, String version, String description, Collection<String> authors) {
    public XeoxMetadata(String id, String name, String version, String description, Collection<String> authors) {
        this.id = id;
        this.name = name == null ? "Name" : name;
        this.version = version == null ? "0" : version;
        this.description = description == null ? "" : description;
        this.authors = authors == null ? Collections.emptyList() : authors;

        if (id == null) {
            throw new IllegalArgumentException("Mod id cannot be null");
        }
    }

    public ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }
}
