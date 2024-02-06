package com.ultreon.craft.resources;

import org.jetbrains.annotations.Nullable;

import java.io.*;

public interface Resource {

    void load();

    default byte @Nullable [] loadOrGet() {
        if (this.isNotLoaded()) this.load();
        if (this.isNotLoaded()) return null;

        return this.getData();
    }

    boolean isLoaded();

    default boolean isNotLoaded() {
        return !this.isLoaded();
    }

    byte[] getData();

    default InputStream openStream() throws IOException {
        byte[] bytes = this.loadOrGet();
        if (bytes == null) throw new IOException("Resource failed to load.");
        return new ByteArrayInputStream(bytes);
    }

    default Reader openReader() throws IOException {
        byte[] bytes = this.loadOrGet();
        if (bytes == null) throw new IOException("Resource failed to load.");
        return new InputStreamReader(new ByteArrayInputStream(bytes));
    }
}
