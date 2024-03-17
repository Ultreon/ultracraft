package com.ultreon.craft.resources;

import com.ultreon.craft.util.Identifier;

import java.util.*;
import java.util.function.BiConsumer;

public class ResourceCategory {
    private final Map<Identifier, StaticResource> resourceMap = new HashMap<>();
    private final String name;

    public ResourceCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void set(Identifier entry, StaticResource resource) {
        this.resourceMap.put(entry, resource);
    }

    public boolean has(Identifier entry) {
        return this.resourceMap.containsKey(entry);
    }

    public StaticResource get(Identifier entry) {
        return this.resourceMap.get(entry);
    }

    public Map<Identifier, StaticResource> mapEntries() {
        return Collections.unmodifiableMap(this.resourceMap);
    }

    public void forEach(BiConsumer<Identifier, StaticResource> consumer) {
        this.resourceMap.forEach(consumer);
    }

    public Set<Identifier> entries() {
        return Collections.unmodifiableSet(this.resourceMap.keySet());
    }

    public int resourceCount() {
        return this.resourceMap.size();
    }

    public List<StaticResource> resources() {
        return List.copyOf(this.resourceMap.values());
    }
}
