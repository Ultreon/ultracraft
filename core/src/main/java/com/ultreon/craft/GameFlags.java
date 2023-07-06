package com.ultreon.craft;

public class GameFlags {
    public static final boolean DEBUG = GamePlatform.instance.isDevelopmentEnvironment();
    public static final boolean ALWAYS_ENABLE_RESET_WORLD = true;
    public static final boolean ENABLE_RESET_WORLD_IN_MOBILE = false;
    public static final boolean ENABLE_RESET_WORLD_IN_DESKTOP = false;
    public static final boolean ENABLE_RESET_WORLD_IN_WEB = false;
}
