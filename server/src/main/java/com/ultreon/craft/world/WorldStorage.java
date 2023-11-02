package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.IType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public final class WorldStorage {
    private final Path directory;

    public WorldStorage(Path path) {
        this.directory = path;
    }

    public WorldStorage(String path) {
        this(Paths.get(path));
    }

    @SafeVarargs
    public final <T extends IType<?>> T read(String path, T... typeGetter) throws IOException {
        Preconditions.checkNotNull(path, "Path is null");
        Preconditions.checkNotNull(typeGetter, "TypeGetter is null");
        return DataIo.readCompressed(this.validatePath(path).toFile(), typeGetter);
    }

    public void write(IType<?> data, String path) throws IOException {
        Preconditions.checkNotNull(data, "Data is null");
        DataIo.writeCompressed(data, this.validatePath(path).toFile());
    }

    public boolean exists(String path) throws IOException {
        Path worldPath = this.validatePath(path);
        return Files.exists(worldPath);
    }

    public void createDir(String path) throws IOException {
        // Validate path
        var worldPath = this.validatePath(path);
        
        // Create the directory if it doesn't exist
        if (Files.notExists(worldPath.getParent(), LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(worldPath.getParent());
        }
    }

    private Path validatePath(String path) throws IOException {
        // Check if the path is in the world directory based on the absolute path.
        if (Paths.get(path).isAbsolute())
            throw new IllegalArgumentException("Path is absolute: " + path);

        Path worldPath = this.directory.resolve(path).toAbsolutePath().normalize();

        // Check if there are any links in the world directory by iterating through the world path.
        for (Path value : worldPath) {
            if (Files.isSymbolicLink(value)) {
                throw new IllegalArgumentException("Path contains symbolic links: " + path);
            }
        }

        // Create parent directories if necessary
        if (Files.notExists(worldPath.getParent(), LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(worldPath.getParent());
        }
        
        return worldPath;
    }

    public Path getDirectory() {
        return this.directory;
    }

    public boolean regionExists(int x, int z) throws IOException {
        return this.exists("regions/" + x + "." + z + ".ucregion");
    }

    public File regionFile(int x, int z) {
        return this.directory.resolve("regions/" + x + "." + z + ".ucregion").toFile();
    }

    @CanIgnoreReturnValue
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public boolean delete() throws IOException {
        if (Files.notExists(this.directory)) return false;
        try (var stream = Files.walk(this.directory)) {
            stream.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            return true;
        }
    }

    public File regionFile(RegionPos pos) {
        return this.regionFile(pos.x(), pos.z());
    }

    public void createWorld() throws IOException {
        this.createDir("regions");
        this.createDir("data");
    }
}
