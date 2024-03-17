package com.ultreon.craft.util;

import com.ultreon.craft.CommonConstants;
import net.fabricmc.loader.api.ModContainer;

public class ModLoadingContext {
    private static ModLoadingContext instance;
    private final ModContainer mod;

    private ModLoadingContext(ModContainer mod) {
        this.mod = mod;
    }

    public static ModLoadingContext get() {
        return instance;
    }

    public static void withinContext(ModContainer mod, Runnable runnable) {
        instance = new ModLoadingContext(mod);
        try {
            runnable.run();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to load mod " + mod.getMetadata().getId(), e);
            throw new RuntimeException(e);
        } finally {
            instance = null;
        }
    }

    public ModContainer getMod() {
        return mod;
    }
}
