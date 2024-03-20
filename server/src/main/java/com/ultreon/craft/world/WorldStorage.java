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

/**
 * The world storage.
 * <p>Represents a world directory.</p>
 */
public final class WorldStorage {
    private final Path directory;

    /**
     * Creates a new world storage instance from the given directory.
     *
     * @param path the world directory.
     */

    public WorldStorage(Path path) {
        this.directory = path;
    }

    /**
     * Creates a new world storage instance from the given directory.
     *
     * @param path the world directory.
     */
    public WorldStorage(String path) {
        this(Paths.get(path));
    }

    /**
     * Creates a new world storage instance from the given directory.
     *
     * @param file the world directory.
     */
    public WorldStorage(File file) {
        this(file.toPath());
    }

    /**
     * Read a UBO object from the given path.
     *
     * @param path       the path to the UBO object.
     * @param typeGetter the type getter. <span style="color: red;">NOTE: do not use this parameter! Leave it empty.</span>
     * @param <T>        the type of the UBO object.
     * @return the UBO object.
     * @throws IOException if an I/O error occurs.
     */
    @SafeVarargs
    public final <T extends IType<?>> T read(String path, T... typeGetter) throws IOException {
        Preconditions.checkNotNull(path, "Path is null");
        Preconditions.checkNotNull(typeGetter, "TypeGetter is null");
        return DataIo.readCompressed(this.validatePath(path).toFile(), typeGetter);
    }

    /**
     * Write a UBO object to the given path.
     *
     * @param data the UBO object to write.
     * @param path the path to the UBO object.
     * @throws IOException if an I/O error occurs.
     */
    public void write(IType<?> data, String path) throws IOException {
        Preconditions.checkNotNull(data, "Data is null");
        DataIo.writeCompressed(data, this.validatePath(path).toFile());
    }

    /**
     * Check if the given path exists.
     *
     * @param path the path to the UBO object.
     * @return {@code true} if the path exists, {@code false} otherwise.
     */
    public boolean exists(String path) {
        try {
            Path worldPath = this.validatePath(path);
            return Files.exists(worldPath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Creates a new subdirectory in the world directory.
     *
     * @param path the relative path to the subdirectory.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * @return the world directory.
     */
    public Path getDirectory() {
        return this.directory;
    }

    /**
     * Check if the given region file exists.
     *
     * @param x the x coordinate of the region.
     * @param z the z coordinate of the region.
     * @return {@code true} if the region file exists, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public boolean regionExists(int x, int z) throws IOException {
        return this.exists("regions/" + x + "." + z + ".ubo");
    }

    /**
     * Get the region file for the given coordinates.
     *
     * @param x the x coordinate of the region.
     * @param z the z coordinate of the region.
     * @return the region file.
     */
    public File regionFile(int x, int z) {
        return this.directory.resolve("regions/" + x + "." + z + ".ubo").toFile();
    }

    /**
     * Delete the world directory.
     *
     * @return {@code true} if the world directory existed before, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * Get the region file for the given coordinates.
     *
     * @param pos the position of the region.
     * @return the region file.
     */
    public File regionFile(RegionPos pos) {
        return this.regionFile(pos.x(), pos.z());
    }

    /**
     * Create the world directory if it doesn't exist.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void createWorld() throws IOException {
        this.createDir("regions");
        this.createDir("data");
    }

    public void loadInfo() {

    }
}
