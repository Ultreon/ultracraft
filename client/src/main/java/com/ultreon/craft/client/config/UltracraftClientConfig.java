package com.ultreon.craft.client.config;

import com.ultreon.craft.GamePlatform;

public class UltracraftClientConfig {
    public int renderDistance = 16;
    public int entityRenderDistance = 12;
    public boolean debugUtils = GamePlatform.get().isDevEnvironment();
    public boolean enable4xScreenshot = true;
    public boolean fullscreen = true;
    public String language = "en_us";
    public FontConfig font = new FontConfig();
    public int fov = 70;
    public int guiScale = 0;

    public static class FontConfig {
        public boolean diagonalFontShadow = true;
        public boolean enforceUnicode = false;
    }
}
