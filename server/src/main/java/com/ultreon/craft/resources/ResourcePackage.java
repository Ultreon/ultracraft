package com.ultreon.craft.resources;

import com.ultreon.craft.util.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResourcePackage {
    protected final Map<Identifier, StaticResource> resources;

    public ResourcePackage(Map<Identifier, StaticResource> resources) {
        this.resources = resources;
    }

    public ResourcePackage() {
        this.resources = new HashMap<>();
    }

    public boolean has(Identifier entry) {
        return this.resources.containsKey(entry);
    }

    public Set<Identifier> entries() {
        return this.resources.keySet();
    }

    public StaticResource get(Identifier entry) {
        return this.resources.get(entry);
    }

    public Map<Identifier, StaticResource> mapEntries() {
        return Collections.unmodifiableMap(this.resources);
    }
}
