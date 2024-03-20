package com.ultreon.craft.resources;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.util.Identifier;
import com.ultreon.libs.commons.v0.util.IOUtils;
import com.ultreon.libs.functions.v0.misc.ThrowingSupplier;
import de.marhali.json5.Json5Element;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StaticResource implements Resource, Closeable {
    private final Identifier id;
    protected ThrowingSupplier<InputStream, IOException> opener;
    private byte[] data;

    public StaticResource(Identifier id, ThrowingSupplier<InputStream, IOException> opener) {
        this.id = id;
        this.opener = opener;
    }

    @Override
    public void load() {
        try (InputStream inputStream = this.opener.get()) {
            this.data = IOUtils.readAllBytes(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLoaded() {
        return this.data != null;
    }

    public InputStream loadOrOpenStream() {
        byte[] buf = this.loadOrGet();
        return buf == null ? null : new ByteArrayInputStream(buf);
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    @Deprecated
    public ByteArrayInputStream openStream() {
        byte[] buf = this.loadOrGet();
        return buf == null ? null : new ByteArrayInputStream(buf);
    }

    public Identifier id() {
        return this.id;
    }

    public @Nullable Json5Element readJson5() {
        byte[] bytes = this.loadOrGet();
        if (bytes == null) return null;

        return CommonConstants.JSON5.parse(new String(bytes, StandardCharsets.UTF_8));
    }

    public void close() {
        this.data = null;
    }
}
