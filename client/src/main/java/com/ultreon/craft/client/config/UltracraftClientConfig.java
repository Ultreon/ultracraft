package com.ultreon.craft.client.config;

import net.fabricmc.loader.api.FabricLoader;

public class UltracraftClientConfig {
    public int renderDistance = 16;
    public int entityRenderDistance = 12;
    public boolean enableDebugUtils = FabricLoader.getInstance().isDevelopmentEnvironment();
    public boolean enable4xScreenshot = true;
}
