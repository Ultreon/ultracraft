package com.ultreon.craft.resources;

import com.ultreon.craft.util.ElementID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResourcePackage {
    protected final Map<ElementID, StaticResource> resources;

    public ResourcePackage(Map<ElementID, StaticResource> resources) {
        this.resources = resources;
    }

    public ResourcePackage() {
        this.resources = new HashMap<>();
    }

    public boolean has(ElementID entry) {
        return this.resources.containsKey(entry);
    }

    public Set<ElementID> entries() {
        return this.resources.keySet();
    }

    public StaticResource get(ElementID entry) {
        return this.resources.get(entry);
    }

    public Map<ElementID, StaticResource> mapEntries() {
        return Collections.unmodifiableMap(this.resources);
    }
}
