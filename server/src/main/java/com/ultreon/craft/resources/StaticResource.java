package com.ultreon.craft.resources;

import com.ultreon.libs.commons.v0.util.IOUtils;
import com.ultreon.libs.functions.v0.misc.ThrowingSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StaticResource implements Resource {
    protected ThrowingSupplier<InputStream, IOException> opener;
    private byte[] data;

    public StaticResource(ThrowingSupplier<InputStream, IOException> opener) {
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
        return new ByteArrayInputStream(this.loadOrGet());
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    @Deprecated
    public ByteArrayInputStream openStream() {
        return new ByteArrayInputStream(this.loadOrGet());
    }
}
