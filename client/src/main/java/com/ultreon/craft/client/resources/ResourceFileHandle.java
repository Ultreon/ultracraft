package com.ultreon.craft.client.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.resources.Resource;
import com.ultreon.craft.util.ElementID;

import java.io.InputStream;
import java.util.UUID;

public class ResourceFileHandle extends FileHandle {
    private final ElementID id;
    private final Resource resource;

    public ResourceFileHandle(ElementID id) {
        super(id.toString());
        this.id = id;
        this.resource = UltracraftClient.get().getResourceManager().getResource(id);
    }

    public ResourceFileHandle(Resource resource) {
        super("generated_" + UUID.randomUUID().toString().replace("-", ""));
        this.id = new ElementID("java", "generated_" + UUID.randomUUID().toString().replace("-", ""));
        this.resource = resource;
    }

    public ElementID getId() {
        return this.id;
    }

    public Resource getResource() {
        return this.resource;
    }

    @Override
    public InputStream read() {
        if (this.resource == null) throw new GdxRuntimeException("Resource %s not found".formatted(this.id));
        return this.resource.loadOrOpenStream();
    }

    @Override
    public boolean exists() {
        return this.resource != null;
    }
}
