package com.ultreon.craft.client.config;

import com.ultreon.craft.config.crafty.ConfigEntry;
import com.ultreon.craft.config.crafty.ConfigInfo;
import com.ultreon.craft.config.crafty.CraftyConfig;
import com.ultreon.craft.config.crafty.Ranged;
import com.ultreon.craft.util.Identifier;

/**
 * This is the client configuration.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@ConfigInfo(fileName = "ultracraft-client")
public class Config extends CraftyConfig {
    @ConfigEntry(path = "video.renderDistance", comment = "The render distance of chunks in the game.")
    @Ranged(min = 4, max = 32)
    public static int renderDistance = 16;

    @ConfigEntry(path = "video.entityRenderDistance", comment = "The render distance of entities.")
    @Ranged(min = 4, max = 32)
    public static int entityRenderDistance = 16;

    @ConfigEntry(path = "video.fog", comment = "Whether fog is enabled.")
    public static boolean fog = true;

    @ConfigEntry(path = "video.ambientOcclusion", comment = "Whether ambient occlusion is enabled.")
    public static boolean ambientOcclusion = true;

    @ConfigEntry(path = "video.enableVsync", comment = "Whether vsync is enabled.\nVsync is when the game updates at the same speed as the monitor updates.")
    public static boolean enableVsync = true;

    @ConfigEntry(path = "video.fpsLimit", comment = "The limit of frames per second.")
    @Ranged(min = 10, max = 240)
    public static int fpsLimit = 60;

    @ConfigEntry(path = "video.fov", comment = "The field of view.")
    @Ranged(min = 40, max = 120)
    public static int fov = 70;

    @ConfigEntry(path = "video.guiScale", comment = "The scale of the GUI.")
    @Ranged(min = 1, max = 4)
    public static int guiScale = 2;

    @ConfigEntry(path = "video.fullscreen", comment = "Whether the game is fullscreen at startup.")
    public static boolean fullscreen = false;

    @ConfigEntry(path = "video.enableFpsCounter", comment = "Whether the FPS counter is enabled in the HUD.")
    public static boolean enableFpsCounter = false;

    @ConfigEntry(path = "generic.enable4xScreenshot", comment = "Whether 4× scaled screenshots are enabled.")
    public static boolean enable4xScreenshot = false;

    @ConfigEntry(path = "generic.enableDebugUtils", comment = "Whether debug utils are enabled.")
    public static boolean enableDebugUtils = false;

    @ConfigEntry(path = "generic.enableDebugOverlay", comment = "Whether debug overlays are enabled.")
    public static boolean enableDebugOverlay = false;

    @ConfigEntry(path = "generic.language", comment = "The language of the game. Use the ISO 639-1 code.")
    public static Identifier language = new Identifier("en_us");

    @ConfigEntry(path = "personalisation.diagonalFontShadow", comment = "Whether diagonal font shadows are enabled.")
    public static boolean diagonalFontShadow = false;

    @ConfigEntry(path = "personalisation.enforceUnicode", comment = "Whether unicode characters are enforced.")
    public static boolean enforceUnicode = false;

    @ConfigEntry(path = "personalisation.blurRadius", comment = "The radius of the blur effect in the background of the GUI.")
    @Ranged(min = 0, max = 120)
    public static float blurRadius = 16.0F;

    @ConfigEntry(path = "personalisation.hexagonTransparency", comment = "The transparency of the hexagon effect in the background of the GUI.")
    @Ranged(min = 0, max = 1)
    public static float hexagonTransparency = 0.5F;

    @ConfigEntry(path = "personalisation.hexagonColor", comment = "The hex color code of the hexagon effect in the background of the GUI.")
    public static String hexagonColor = "#FFFFFF";

    @ConfigEntry(path = "personalisation.backgroundTransparency", comment = "The transparency of in-game menu backgrounds.")
    @Ranged(min = 0, max = 1)
    public static float backgroundTransparency = 0.5F;

    @ConfigEntry(path = "crafting.showOnlyCraftable", comment = "Whether only craftable items are shown in the inventory.")
    @Ranged(min = 0, max = 1)
    public static boolean showOnlyCraftable = true;

    @ConfigEntry(path = "accessibility.hideFirstPersonPlayer", comment = "Whether the first person player is hidden.")
    public static boolean hideFirstPersonPlayer = true;

    @ConfigEntry(path = "accessibility.hideHotbarWhenThirdPerson", comment = "Whether the hotbar is hidden when in third person.")
    public static boolean hideHotbarWhenThirdPerson = false;

    @ConfigEntry(path = "accessibility.vibration", comment = "Whether the controller vibration is enabled in the game.")
    public static boolean vibration = true;

    @ConfigEntry(path = "accessibility.enableHud", comment = "Whether the HUD is enabled in the game.")
    public static boolean enableHud = true;

    @ConfigEntry(path = "accessibility.enableCrosshair", comment = "Whether the crosshair is enabled in the game's HUD.")
    public static boolean enableCrosshair = true;

    @ConfigEntry(path = "privacy.hideUsername", comment = "Whether your username is hidden for other players in multiplayer.")
    public static boolean hideUsername = true;

    @ConfigEntry(path = "privacy.hideSkin", comment = "Whether your skin is hidden for other players in multiplayer.")
    public static boolean hideSkin = true;

    @ConfigEntry(path = "privacy.hideActiveServerFromRPC", comment = "Whether the active server is hidden from the Discord Rich Presence.")
    public static boolean hideActiveServerFromRPC = true;

    @ConfigEntry(path = "privacy.hideRPC", comment = "Whether the Discord Rich Presence is hidden.")
    public static boolean hideRPC = true;

    @ConfigEntry(path = "audio.enableSound", comment = "Whether sound is enabled in the game.")
    public static boolean enableSound = true;

    @ConfigEntry(path = "audio.enableMusic", comment = "Whether music is enabled in the game.")
    public static boolean enableMusic = true;

    @ConfigEntry(path = "audio.soundVolume", comment = "The volume of sound effects in the game.")
    @Ranged(min = 0, max = 100)
    public static int soundVolume = 100;

    @ConfigEntry(path = "audio.musicVolume", comment = "The volume of music in the game.")
    @Ranged(min = 0, max = 100)
    public static int musicVolume = 100;

    @ConfigEntry(path = "audio.masterVolume", comment = "The master volume of the game's audio.")
    @Ranged(min = 0, max = 100)
    public static int masterVolume = 100;
}
