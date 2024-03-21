package com.ultreon.craft.resources;

import com.ultreon.craft.util.Identifier;

import java.io.Closeable;
import java.util.*;

public class ResourcePackage implements Closeable {
    protected final Map<Identifier, StaticResource> resources;
    protected final Map<String, ResourceCategory> categories;
    private boolean locked;

    public ResourcePackage(Map<Identifier, StaticResource> resources, Map<String, ResourceCategory> categories) {
        this.resources = resources;
        this.categories = categories;
    }

    public ResourcePackage() {
        this.resources = new HashMap<>();
        this.categories = new HashMap<>();
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

    public boolean hasCategory(String name) {
        return this.categories.containsKey(name);
    }

    public ResourceCategory getCategory(String name) {
        return this.categories.get(name);
    }

    public List<ResourceCategory> getCategories() {
        return List.copyOf(this.categories.values());
    }

    public void close() {
        for (StaticResource resource : this.resources.values()) {
            resource.close();
        }
    }
}
