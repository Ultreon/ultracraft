package com.ultreon.craft.client.config;

import net.fabricmc.loader.api.FabricLoader;

public class UltracraftClientConfig {
    public int renderDistance = 16;
    public int entityRenderDistance = 12;
    public boolean debugUtils = FabricLoader.getInstance().isDevelopmentEnvironment();
    public boolean enable4xScreenshot = true;
    public boolean fullscreen = true;
    public String language = "en_us";
    public FontConfig font = new FontConfig();
    public int fov = 70;

    public static class FontConfig {
        public boolean diagonalFontShadow = true;
        public boolean enforceUnicode = false;
    }
}
