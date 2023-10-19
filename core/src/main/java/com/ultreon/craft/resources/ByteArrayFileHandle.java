package com.ultreon.craft.resources;

import com.badlogic.gdx.files.FileHandle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
}
