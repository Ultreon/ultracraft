package com.ultreon.craft;

import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;

public class DataFileHandle extends FileHandle {
    public DataFileHandle(String path) {
        super(GamePlatform.data(path).file());
    }
}
