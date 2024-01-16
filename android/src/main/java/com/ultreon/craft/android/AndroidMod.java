package com.ultreon.craft.android;

import com.ultreon.craft.Mod;
import com.ultreon.craft.ModOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/** @noinspection ClassCanBeRecord*/
public class AndroidMod implements Mod {
    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final Collection<String> authors;

    public AndroidMod(String id, String name, String version, String description, Collection<String> authors) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.authors = authors;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return authors;
    }

    @Override
    public ModOrigin getOrigin() {
        return ModOrigin.ACTUAL_PATH;
    }

    @Override
    @Nullable
    public Iterable<Path> getRootPaths() {
        return null;
    }

    @Override
    public Optional<String> getIconPath(int size) {
        return Optional.empty();
    }
}
