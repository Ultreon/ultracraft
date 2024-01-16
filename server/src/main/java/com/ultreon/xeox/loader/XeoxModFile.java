package com.ultreon.xeox.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Represents a mod file.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see XeoxMod
 */
public class XeoxModFile {
    private static final Gson GSON = new Gson();
    private final FileHandle file;
    private final XeoxMetadata metadata;

    @Internal
    public XeoxModFile(FileHandle file) {
        this.file = file;

        this.metadata = GSON.fromJson(file.child("xeox.metadata.json").readString(), XeoxMetadata.class);
    }

    /**
     * Imports a mod file from the given file.
     * 
     * @param file                the mod file
     * @return                    the mod
     * @throws IOException        if an I/O error occurs
     * @throws ModImportException if the mod could not be imported
     */
    @CanIgnoreReturnValue
    public static XeoxModFile importFile(File file) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            // Get Xeox Metadata file
            ZipEntry metadataEntry = zip.getEntry("xeox.metadata.json");
            if (metadataEntry == null) {
                throw new ModImportException("Missing metadata file.");
            }

            InputStream inputStream = zip.getInputStream(metadataEntry);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            var metadata = GSON.fromJson(inputStreamReader, XeoxMetadata.class);
            inputStreamReader.close();

            validateMetadata(metadata);
            FileHandle external = Gdx.files.external("mods/" + metadata.id());

            if (external.exists()) {
                throw new ModImportException("Mod already exists.");
            }
            external.mkdirs();

            Enumeration<? extends ZipEntry> entries = zip.entries();
            if (!entries.hasMoreElements()) {
                throw new ModImportException("No mod files found.");
            }
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream in = zip.getInputStream(entry);
                FileHandle out = Gdx.files.external("mods/" + metadata.id() + "/" + entry.getName());
                out.writeBytes(com.ultreon.libs.commons.v0.util.IOUtils.readAllBytes(in), false);
                in.close();
            }

            return new XeoxModFile(external);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new ModImportException("Invalid metadata file.", e);
        }
    }

    private static void validateMetadata(XeoxMetadata metadata) throws IOException {
        if (metadata.id() == null) throw new ModImportException("Missing mod id.");
        if (metadata.name() == null) throw new ModImportException("Missing mod name.");
    }

    public XeoxMetadata getMetadata() {
        return metadata;
    }

    public FileHandle getFile() {
        return file;
    }

    public XeoxMod constructMod() {
        return new XeoxMod(metadata, file.path());
    }
}
