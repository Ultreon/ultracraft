package com.ultreon.craft.client.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.resources.v0.Resource;

import java.io.InputStream;
import java.util.UUID;

public class ResourceFileHandle extends FileHandle {
    private final Identifier id;
    private final Resource resource;

    public ResourceFileHandle(Identifier id) {
        super(id.toString());
        this.id = id;
        this.resource = UltracraftClient.get().getResourceManager().getResource(id);
    }

    public ResourceFileHandle(Resource resource) {
        super("generated_" + UUID.randomUUID().toString().replaceAll("-", ""));
        this.id = new Identifier("java", "generated_" + UUID.randomUUID().toString().replaceAll("-", ""));
        this.resource = resource;
    }

    public Identifier getId() {
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
