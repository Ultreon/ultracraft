package com.ultreon.craft;

import com.ultreon.craft.util.Env;
import com.ultreon.xeox.loader.XeoxLoader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class GamePlatform {
    protected static GamePlatform instance;

    protected GamePlatform() {
        instance = this;
    }

    public static GamePlatform get() {
        return GamePlatform.instance;
    }

    public void preInitImGui() {
        // Implemented in subclasses
    }

    public void setupImGui() {
        // Implemented in subclasses
    }

    public void renderImGui() {
        // Implemented in subclasses
    }

    public void onFirstRender() {
        // Implemented in subclasses
    }

    public void onGameDispose() {
        // Implemented in subclasses
    }

    public boolean isShowingImGui() {
        return false;
    }

    public void setShowingImGui(boolean value) {
        // Implemented in subclasses
    }

    public boolean areChunkBordersVisible() {
        return false;
    }

    public boolean showRenderPipeline() {
        return true;
    }

    /**
     * Get the mod metadata by id
     *
     * @param id game mod id
     * @return the mod metadata
     */
    public Optional<Mod> getMod(String id) {
        return XeoxLoader.get().getMod(id);
    }

    public boolean isModLoaded(String id) {
        return XeoxLoader.get().isModLoaded(id);
    }

    public Collection<? extends Mod> getMods() {
        return XeoxLoader.get().getMods();
    }

    public boolean isDevEnvironment() {
        return false;
    }

    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        // Implemented in subclasses
    }

    public Env getEnv() {
        return Env.CLIENT;
    }

    public Path getConfigDir() {
        return Paths.get("config");
    }

    public Path getGameDir() {
        return Paths.get(".");
    }

    public boolean isMobile() {
        // Implemented in subclasses
        return false;
    }

    public boolean openImportDialog() {
        // Implemented in subclasses
        return false;
    }

    public void prepare() {
        // Implemented in subclasses
    }

    public boolean isDesktop() {
        return false;
    }

    public boolean hasCompass() {
        return false;
    }

    public Context enterXeoxContext() {
        Context context = ContextFactory.getGlobal().enterContext();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_ES6);
        return context;
    }
}
