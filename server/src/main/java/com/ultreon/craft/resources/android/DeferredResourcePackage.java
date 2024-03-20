package com.ultreon.craft.resources.android;

import com.ultreon.craft.resources.StaticResource;
import com.ultreon.craft.resources.ResourcePackage;
import com.ultreon.craft.util.Identifier;
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
    public boolean has(Identifier entry) {
        return this.getUrl(entry) != null;
    }

    private URL getUrl(Identifier entry) {
        return this.ref.getResource(this.getPath(entry));
    }

    @NotNull
    private String getPath(Identifier entry) {
        return "/" + this.root + "/" + entry.namespace() + "/" + entry.path();
    }

    @Override
    public StaticResource get(Identifier entry) {
        if (!this.has(entry)) return null;
        if (this.resources.containsKey(entry)) return this.resources.get(entry);

        StaticResource resource = new StaticResource(entry, () -> this.ref.getResourceAsStream(this.getPath(entry)));
        this.resources.put(entry, resource);
        return resource;
    }
}
