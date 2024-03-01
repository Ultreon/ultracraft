package com.ultreon.craft.client.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.common.base.Preconditions;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.resources.Resource;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ResourceFileHandle extends FileHandle {
    private final Identifier id;
    private final @Nullable Resource resource;

    public ResourceFileHandle(Identifier id) {
        super(id.toString());
        this.id = id;
        this.resource = UltracraftClient.get().getResourceManager().getResource(id);
    }

    public ResourceFileHandle(@NotNull Resource resource) {
        super("generated_" + UUID.randomUUID().toString().replace("-", ""));

        Preconditions.checkNotNull(resource, "resource");

        this.id = new Identifier("java", "generated_" + UUID.randomUUID().toString().replace("-", ""));
        this.resource = resource;
    }

    public Identifier getId() {
        return this.id;
    }

    public @Nullable Resource getResource() {
        return this.resource;
    }

    @Override
    public InputStream read() {
        if (this.resource == null) throw new GdxRuntimeException("Resource %s not found".formatted(this.id));
        try {
            return this.resource.openStream();
        } catch (IOException e) {
            throw new GdxRuntimeException("An IO error occurred while reading resource %s".formatted(this.id), e);
        }
    }

    @Override
    public boolean exists() {
        return this.resource != null;
    }
}
