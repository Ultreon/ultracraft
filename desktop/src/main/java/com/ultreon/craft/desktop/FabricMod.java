package com.ultreon.craft.desktop;

import com.ultreon.craft.Mod;
import com.ultreon.craft.ModOrigin;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements {@link Mod} for {@link ModContainer}
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class FabricMod implements Mod {
    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final Collection<String> authors;
    private final ModMetadata metadata;
    private final ModContainer container;

    public FabricMod(ModContainer container) {
        metadata = container.getMetadata();
        this.container = container;
        this.id = metadata.getId();
        this.name = metadata.getName();
        this.version = metadata.getVersion().getFriendlyString();
        this.description = metadata.getDescription();
        this.authors = metadata.getAuthors().stream().map(Person::getName).collect(Collectors.toList());
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
    public Optional<String> getIconPath(int size) {
        return this.metadata.getIconPath(size);
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return authors;
    }

    @Override
    public ModOrigin getOrigin() {
        return switch (container.getOrigin().getKind()) {
            case PATH -> ModOrigin.ACTUAL_PATH;
            case NESTED -> ModOrigin.BUNDLED;
            case UNKNOWN -> ModOrigin.OTHER;
        };
    }

    @Override
    public Iterable<Path> getRootPaths() {
        return null;
    }
}
