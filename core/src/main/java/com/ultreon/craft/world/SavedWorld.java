package com.ultreon.craft.world;

import com.ultreon.data.DataIo;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.MapType;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("ClassCanBeRecord")
public final class SavedWorld {
    private final File directory;

    public SavedWorld(File directory) {
        this.directory = directory;
    }

    @SafeVarargs
    public final <T extends IType<?>> T read(String path, T... type) throws IOException {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return DataIo.readCompressed(new File(this.directory, path), type);
    }

    public void write(IType<?> data, String path) throws IOException {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        DataIo.writeCompressed(data, new File(this.directory, path));
    }

    public boolean exists(String path) {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return new File(this.directory, path).exists();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createDir(String path) throws IOException {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        File file = new File(this.directory, path);
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Failed to create directory: " + file.getPath());
        }
    }

    public File getDirectory() {
        return this.directory;
    }

    public boolean chunkExists(int x, int z) {
        return this.exists("chunks/c" + x + "." + z + ".ubo");
    }

    public MapType readChunk(int x, int z) throws IOException {
        return this.read("chunks/c" + x + "." + z + ".ubo");
    }

    public void writeChunk(int x, int z, MapType data) throws IOException {
        this.write(data, "chunks/c" + x + "." + z + ".ubo");
    }
}
