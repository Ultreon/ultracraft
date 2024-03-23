package com.ultreon.craft.resources;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ultreon.craft.CommonConstants;
import de.marhali.json5.Json5Element;
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

    default <T> T loadJson(Class<T> jsonObjectClass) {
        byte[] bytes = this.loadOrGet();
        if (bytes == null) throw new RuntimeException("Resource failed to load.");

        return new GsonBuilder().create().fromJson(new String(bytes), jsonObjectClass);
    }

    default Json5Element loadJson5() {
        byte[] bytes = this.loadOrGet();
        if (bytes == null) throw new RuntimeException("Resource failed to load.");

        return CommonConstants.JSON5.parse(new String(bytes));
    }
}
