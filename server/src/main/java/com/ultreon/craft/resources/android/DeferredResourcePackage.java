package com.ultreon.craft.resources.android;

import com.ultreon.craft.resources.ResourcePackage;
import com.ultreon.craft.resources.StaticResource;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.HashMap;

public class DeferredResourcePackage extends ResourcePackage {
    private final Class<?> ref;
    private final String root;

    public DeferredResourcePackage(Class<?> ref, String root) {
        super(new HashMap<>());
        this.ref = ref;
        this.root = root;
    }

    @Override
    public boolean has(ElementID entry) {
        return this.getUrl(entry) != null;
    }

    private URL getUrl(ElementID entry) {
        return this.ref.getResource(this.getPath(entry));
    }

    @NotNull
    private String getPath(ElementID entry) {
        return "/" + this.root + "/" + entry.namespace() + "/" + entry.path();
    }

    @Override
    public StaticResource get(ElementID entry) {
        if (!this.has(entry)) return null;
        if (this.resources.containsKey(entry)) return this.resources.get(entry);

        StaticResource resource = new StaticResource(() -> this.ref.getResourceAsStream(this.getPath(entry)));
        this.resources.put(entry, resource);
        return resource;
    }
}
