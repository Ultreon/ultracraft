package com.ultreon.craft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * This interface represents a mod.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public interface Mod {
    /**
     * Get the id of the mod.
     * 
     * @return the mod id
     */
    @NotNull
    String getId();

    /**
     * Get the name of the mod.
     * 
     * @return the mod name
     */
    @NotNull
    String getName();

    /**
     * Get the version of the mod.
     * 
     * @return the mod version
     */
    @NotNull
    String getVersion();

    /**
     * Get the description of the mod.
     * 
     * @return the mod description or null if unavailable
     */
    @Nullable
    String getDescription();

    /**
     * Get the icon path of the mod for the given size.
     * 
     * @param size the size
     * @return the icon path or an empty optional if unavailable
     */
    @NotNull
    default Optional<String> getIconPath(int size) {
        return Optional.empty();
    }

    /**
     * Get the authors of the mod.
     * 
     * @return the mod authors or an empty collection if unknown/annonymous.
     */
    @NotNull
    Collection<String> getAuthors();

    /**
     * Get the mod's origin, e.g. {@link ModOrigin#ACTUAL_PATH}.
     * 
     * @return the mod's origin
     */
    @NotNull
    ModOrigin getOrigin();

    /**
     * Get the root paths of the mod.
     * 
     * @return The root paths of the mod, or null if not supported.
     */
    @Nullable
    Iterable<Path> getRootPaths();
}
