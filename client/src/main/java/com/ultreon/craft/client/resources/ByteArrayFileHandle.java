package com.ultreon.craft.client.resources;

import com.badlogic.gdx.files.FileHandle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

public class ByteArrayFileHandle extends FileHandle {
    private final byte[] data;

    public ByteArrayFileHandle(String extension, byte[] data) {
        super("generated " + UUID.randomUUID() + extension);
        this.data = data;
    }

    @Override
    public InputStream read() {
        return new ByteArrayInputStream(this.data);
    }

    @Override
    public byte[] readBytes() {
        return this.data.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ByteArrayFileHandle that = (ByteArrayFileHandle) o;
        return Arrays.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(this.data);
        return result;
    }
}
