package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.MapType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@SuppressWarnings("ClassCanBeRecord")
public final class WorldStorage {
    private final File directory;

    public WorldStorage(File directory) {
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
        Preconditions.checkNotNull(data, "Data is null");
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

    public void createDir(String path) {
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        File file = new File(this.directory, path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public File getDirectory() {
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
        Preconditions.checkNotNull(data, "Chunk data is null");
        this.write(data, "chunks/c" + x + "." + z + ".ubo");
    }

    public boolean regionExists(int x, int z) {
        return this.exists("regions/" + x + "." + z + ".ucregion");
    }

    public File regionFile(int x, int z) {
        return new File(this.directory, "regions/" + x + "." + z + ".ucregion");
    }

    @Deprecated
    public MapType readRegion(int x, int z) throws IOException {
        return this.read("regions/r" + x + "." + z + ".ucregion");
    }

    @Deprecated
    public void writeRegion(int x, int z, MapType data) throws IOException {
        Preconditions.checkNotNull(data, "Region data is null");
        this.write(data, "regions/r" + x + "." + z + ".ucregion");
    }

    @CanIgnoreReturnValue
    @SuppressWarnings({"ResultOfMethodCallIgnored", "resource"})
    public boolean delete() throws IOException {
        if (!this.directory.exists()) return false;
        Files.walk(this.directory.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        return true;
    }

    public File regionFile(RegionPos pos) {
        return this.regionFile(pos.x(), pos.z());
    }
}
