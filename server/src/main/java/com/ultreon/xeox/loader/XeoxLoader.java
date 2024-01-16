package com.ultreon.xeox.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.Mod;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The Xeox modloader for Ultracraft.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see XeoxMod
 */
public class XeoxLoader {
    public static final Logger LOGGER = LoggerFactory.getLogger("XeoxLoader");
    private static final XeoxLoader instance = new XeoxLoader();
    private final FileHandle modsDir = Gdx.files.external("mods");
    private final List<ModImportException> errors = new ArrayList<>();
    private final List<RhinoException> rhinoExceptions = new ArrayList<>();
    private final List<XeoxModFile> modFiles = new ArrayList<>();
    private final Map<String, XeoxMod> mods = new IdentityHashMap<>();

    private XeoxLoader() {

    }

    /**
     * Get the singleton instance
     * 
     * @return the XeoxLoader instance
     */
    public static XeoxLoader get() {
        return instance;
    }

    /**
     * Loads all mods from the mods directory.
     * 
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     * @since 0.1.0
     */
    @Internal
    public void loadMods() {
        XeoxEvents.EVENT_REGISTRATION.factory().onRegister();

        for (FileHandle fileHandle : modsDir.list()) {
            if (!fileHandle.isDirectory()) {
                continue;
            }
            this.modFiles.add(new XeoxModFile(fileHandle));
        }
    }

    /**
     * Constructs all mods from the mod files.
     * 
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     * @since 0.1.0
     */
    @Internal
    public void constructMods() {
        for (XeoxModFile modFile : this.modFiles) {
            XeoxMod xeoxMod = modFile.constructMod();
            this.mods.put(xeoxMod.getId(), xeoxMod);
        }
    }

    /**
     * Initializes all mods.
     * 
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     * @since 0.1.0
     */
    @Internal
    public void initMods() {
        for (XeoxMod mod : this.mods.values()) {
            try {
                mod.init();
            } catch (RhinoException e) {
                LOGGER.error("Failed to initialize mod {}:", mod.getMetadata().id(), e);
                recordError(e);
            } catch (Exception e) {
                LOGGER.error("Failed to initialize mod {}", mod.getMetadata().id(), e);
            }
        }
    }
    
    /**
     * Records an error to be shown later.
     * This is only used for rhino exceptions.
     * 
     * @param e the error
     */
    private void recordError(RhinoException e) {
        this.rhinoExceptions.add(e);
    }

    /**
     * Imports a mod into the mods directory.
     * 
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     * @since 0.1.0
     */
    public void importMod(File file) {
        try {
            XeoxModFile.importFile(file);
        } catch (IOException e) {
            LOGGER.error("Failed to import mod {}.", file.getName(), e);
        }
    }

    /**
     * Get all errors that occurred during importing.
     * 
     * @return a list of import errors
     */
    public List<ModImportException> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }
    
    /**
     * Get all rhino exceptions that occurred during modloading.
     * 
     * @return a list of rhino exceptions
     */
    public List<RhinoException> getRhinoExceptions() {
        return Collections.unmodifiableList(this.rhinoExceptions);
    }

    /**
     * Get a mod by its id.
     * 
     * @param id the mod id
     * @return   the mod object.
     */
    public Optional<Mod> getMod(String id) {
        return Optional.ofNullable(this.mods.get(id));
    }

    /**
     * Check if a mod is loaded.
     * 
     * @param id the mod id
     * @return   true if the mod is loaded, false otherwise.
     */
    public boolean isModLoaded(String id) {
        return this.mods.containsKey(id);
    }

    /**
     * Get all loaded mods.
     * 
     * @return a list of loaded mods
     */
    public List<XeoxMod> getMods() {
        return List.copyOf(this.mods.values());
    }
}
