package com.ultreon.craft.resources;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.resources.android.DeferredResourcePackage;
import com.ultreon.craft.util.ElementID;
import com.ultreon.libs.commons.v0.Logger;
import com.ultreon.libs.commons.v0.exceptions.SyntaxException;
import com.ultreon.libs.commons.v0.util.IOUtils;
import com.ultreon.libs.functions.v0.misc.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceManager {
    protected final List<com.ultreon.craft.resources.ResourcePackage> resourcePackages = new ArrayList<>();
    public static Logger logger = (level, msg, t) -> {};
    private final String root;

    public ResourceManager(String root) {
        this.root = root;
    }

    public boolean canScanFiles() {
        return true;
    }

    public InputStream openResourceStream(ElementID entry) throws IOException {
        @Nullable Resource resource = this.getResource(entry);
        return resource == null ? null : resource.openStream();
    }

    @Nullable
    public Resource getResource(ElementID entry) {
        for (com.ultreon.craft.resources.ResourcePackage resourcePackage : this.resourcePackages) {
            if (resourcePackage.has(entry)) {
                return resourcePackage.get(entry);
            }
        }

        logger.warn("Unknown resource: " + entry);


        return null;
    }

    public void importDeferredPackage(Class<?> ref) {
        this.resourcePackages.add(new DeferredResourcePackage(ref, this.root));
    }

    public void importPackage(URI uri) throws IOException {
        URL url = uri.toURL();
        if (url.getProtocol().equals("file")) {
            this.importPackage(new File(uri));
        } else if (url.getProtocol().equals("jar")) {
            try {
                this.importFilePackage(new ZipInputStream(new URI(uri.toURL().getPath().split("!/", 2)[0]).toURL().openStream()), uri.toASCIIString());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI: " + uri, e);
            }
        } else {
            this.importFilePackage(new ZipInputStream(uri.toURL().openStream()), uri.toASCIIString());
        }
    }

    public void importPackage(Path path) throws IOException {
        this.importPackage(path.toUri());
    }

    public void importPackage(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Resource package doesn't exists: " + file.getAbsolutePath());
        }

        if (file.isFile()) {
            if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
                this.importFilePackage(new ZipInputStream(Files.newInputStream(file.toPath())), file.getAbsolutePath());
            } else {
                logger.warn("Resource package isn't a .jar or .zip file: " + file.getPath());
            }
        } else if (file.isDirectory()) {
            this.importDirectoryPackage(file);
        }
    }

    @SuppressWarnings({"unused"})
    private void importDirectoryPackage(File file) {
        // Check if it's a directory.
        assert file.isDirectory();

        try {
            // Prepare (entry -> resource) mappings/
            Map<ElementID, StaticResource> map = new HashMap<>();

            // Get assets directory.
            File assets = new File(file, this.root + "/");

            // Check if the assets directory exists.
            if (assets.exists()) {
                // List files in assets dir.
                File[] files = assets.listFiles();

                // Loop listed files.
                for (File resPackage : files != null ? files : new File[0]) {
                    // Get assets-package namespace from the name create the listed file (that's a dir).
                    String namespace = resPackage.getName();

                    // Walk assets package.
                    try (Stream<Path> walk = Files.walk(resPackage.toPath())) {
                        for (Path assetPath : walk.collect(Collectors.toList())) {
                            // Convert to a file object.
                            File asset = assetPath.toFile();

                            // Check if it's a file, if not,
                            // we will walk to the next file / folder in the Files.walk(...)
                            // list.
                            if (!asset.isFile()) {
                                continue;
                            }

                            // Continue to the next file / folder
                            // if the asset path is the same path as the resource package.
                            if (assetPath.toFile().equals(resPackage)) {
                                continue;
                            }

                            // Calculate resource path.
                            Path relative = resPackage.toPath().relativize(assetPath);
                            String s = relative.toString().replaceAll("\\\\", "/");

                            // Create resource entry/
                            ElementID entry;
                            try {
                                entry = new ElementID(namespace, s);
                            } catch (SyntaxException e) {
                                logger.error("Invalid resource identifier:", e);
                                continue;
                            }

                            // Create resource with file input stream.
                            ThrowingSupplier<InputStream, IOException> sup = () -> Files.newInputStream(asset.toPath());
                            StaticResource resource = new StaticResource(entry, sup);

                            // Add resource mapping for (entry -> resource).
                            map.put(entry, resource);
                        }
                    }
                }

                this.resourcePackages.add(new com.ultreon.craft.resources.ResourcePackage(map));
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load resource package: " + file.getAbsolutePath(), e);
        }
    }

    private void importFilePackage(ZipInputStream stream, String filePath) throws IOException {
        // Check for .jar files.
        // Prepare (entry -> resource) mappings.
        Map<ElementID, StaticResource> map = new HashMap<>();

        // Create jar file instance from file.
        try {
            // Loop jar entries.
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                // Get name to create the jar entry.
                String name = entry.getName();
                byte[] bytes = IOUtils.readAllBytes(stream);
                ThrowingSupplier<InputStream, IOException> sup = () -> new ByteArrayInputStream(bytes);

                // Check if it isn't a directory, because we want a file.
                if (!entry.isDirectory()) {
                    this.addEntry(map, name, sup);
                }
                stream.closeEntry();
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load resource package: " + filePath, e);
        }

        this.resourcePackages.add(new com.ultreon.craft.resources.ResourcePackage(map));

        stream.close();
    }

    private void addEntry(Map<ElementID, StaticResource> map, String name, ThrowingSupplier<InputStream, IOException> sup) {
        String[] splitPath = name.split("/", 3);

        if (splitPath.length >= 3) {
            if (name.startsWith(this.root + "/")) {
                // Get namespace and path from the split path
                String namespace = splitPath[1];
                String path = splitPath[2];

                // Entry
                ElementID entry = new ElementID(namespace, path);

                // Resource
                StaticResource resource = new StaticResource(entry, sup);

                try {

                    // Add (entry -> resource) mapping.
                    map.put(entry, resource);
                } catch (Throwable ignored) {

                }
            }
        }
    }

    @NotNull
    public List<byte[]> getAllDataByPath(@NotNull String path) {
        List<byte[]> data = new ArrayList<>();
        for (com.ultreon.craft.resources.ResourcePackage resourcePackage : this.resourcePackages) {
            Map<ElementID, StaticResource> identifierResourceMap = resourcePackage.mapEntries();
            for (Map.Entry<ElementID, StaticResource> entry : identifierResourceMap.entrySet()) {
                if (entry.getKey().path().equals(path)) {
                    byte[] bytes = entry.getValue().loadOrGet();
                    if (bytes == null) continue;

                    data.add(entry.getValue().getData());
                }
            }
        }

        return data;
    }

    @NotNull
    public List<byte[]> getAllDataById(@NotNull ElementID id) {
        List<byte[]> data = new ArrayList<>();
        for (ResourcePackage resourcePackage : this.resourcePackages) {
            if (resourcePackage.has(id)) {
                StaticResource resource = resourcePackage.get(id);
                if (resource == null) continue;
                byte[] bytes = resource.loadOrGet();
                if (bytes == null) continue;

                data.add(resource.getData());
            }
        }

        return data;
    }

    public String getRoot() {
        return this.root;
    }
}
