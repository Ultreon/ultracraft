package com.ultreon.craft.client.config;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.util.Identifier;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.UnknownNullability;

public class UltracraftClientConfig {
    public int renderDistance = 16;
    public int entityRenderDistance = 12;
    public boolean debugUtils = FabricLoader.getInstance().isDevelopmentEnvironment();
    public boolean enable4xScreenshot = true;
    public @UnknownNullability Identifier language = UltracraftClient.id("en_us");
    public @UnknownNullability PersonalisationConfig personalisation = new PersonalisationConfig();
    public @UnknownNullability CraftingConfig crafting = new CraftingConfig();
    public @UnknownNullability AccessibilityConfig accessibility = new AccessibilityConfig();
    public @UnknownNullability PrivacyConfig privacy = new PrivacyConfig();
    public @UnknownNullability VideoConfig video = new VideoConfig();

    public static class PersonalisationConfig {
        public boolean diagonalFontShadow = false;
        public boolean enforceUnicode = false;
    }

    public static class CraftingConfig {
        public boolean showOnlyCraftable = true;
    }

    public static class AccessibilityConfig {
        public boolean hideFirstPersonPlayer = true;
        public boolean hideHotbarWhenThirdPerson = false;
        public boolean vibration = true;
    }

    public static class PrivacyConfig {
        public boolean hidePlayerName = false;
        public boolean hidePlayerSkin = false;
        public boolean hideActiveServer = true;
        public boolean hideRpc = true;
    }

    public static class VideoConfig {
        public boolean enableVsync = true;
        public int fpsLimit = 60;
        public int fov = 70;
        public int guiScale = 0;
        public boolean fullscreen = false;
    }
}
