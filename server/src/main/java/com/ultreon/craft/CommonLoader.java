package com.ultreon.craft;

import com.ultreon.craft.config.crafty.CraftyConfig;
import com.ultreon.craft.events.ConfigEvents;
import com.ultreon.craft.util.ModLoadingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Class responsible for initializing configuration entry points.
 */
public class CommonLoader {

    /**
     * Initialize configuration entry points.
     * @param loader the FabricLoader instance
     */
    public static void initConfigEntrypoints(FabricLoader loader) {
        // Load configurations from entry points.
        for (var container : loader.getEntrypointContainers(CraftyConfig.ENTRYPOINT_KEY, CraftyConfig.class)) {
            ModContainer provider = container.getProvider();
            CraftyConfig entrypoint = container.getEntrypoint();

            if (provider != null && entrypoint != null)
                ModLoadingContext.withinContext(provider, entrypoint::load);
        }

        // Trigger event for config load on the client side.
        ConfigEvents.LOAD.factory().onConfigLoad(EnvType.CLIENT);
    }
}
