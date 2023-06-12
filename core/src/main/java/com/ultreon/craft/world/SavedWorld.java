package com.ultreon.craft.world;

import com.badlogic.gdx.files.FileHandle;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.MapType;

import java.io.IOException;

@SuppressWarnings("ClassCanBeRecord")
public final class SavedWorld {
    private final FileHandle directory;

    public SavedWorld(FileHandle directory) {
        this.directory = directory;
    }

    @SafeVarargs
    public final <T extends IType<?>> T read(String path, T... type) throws IOException {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return DataIo.readCompressed(this.directory.child(path).read(), type);
    }

    public void write(IType<?> data, String path) throws IOException {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        DataIo.writeCompressed(data, this.directory.child(path).write(false));
    }

    public boolean exists(String path) {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return this.directory.child(path).exists();
    }

    public void createDir(String path) {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        FileHandle file = this.directory.child(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public FileHandle getDirectory() {
        return this.directory;
    }

    @Deprecated
    public boolean chunkExists(int x, int z) {
        return this.exists("chunks/c" + x + "." + z + ".ubo");
    }

    @Deprecated
    public MapType readChunk(int x, int z) throws IOException {
        return this.read("chunks/c" + x + "." + z + ".ubo");
    }

    @Deprecated
    public void writeChunk(int x, int z, MapType data) throws IOException {
        this.write(data, "chunks/c" + x + "." + z + ".ubo");
    }

    public boolean regionExists(int x, int z) {
        return this.exists("regions/r" + x + "." + z + ".ubo");
    }

    public MapType readRegion(int x, int z) throws IOException {
        return this.read("regions/r" + x + "." + z + ".ubo");
    }

    public void writeRegion(int x, int z, MapType data) throws IOException {
        this.write(data, "regions/r" + x + "." + z + ".ubo");
    }

    public void delete() {
        this.directory.deleteDirectory();
    }
}
