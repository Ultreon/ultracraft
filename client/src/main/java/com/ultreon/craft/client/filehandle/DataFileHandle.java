package com.ultreon.craft.client.filehandle;

import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.client.UltracraftClient;

public class DataFileHandle extends FileHandle {
    public DataFileHandle(String path) {
        super(UltracraftClient.data(path).file());
    }
}
