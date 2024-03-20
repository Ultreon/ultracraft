package com.ultreon.craft.resources;

import com.ultreon.craft.util.Identifier;
import com.ultreon.libs.commons.v0.util.IOUtils;
import com.ultreon.libs.functions.v0.misc.ThrowingSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StaticResource implements Resource {
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
}
