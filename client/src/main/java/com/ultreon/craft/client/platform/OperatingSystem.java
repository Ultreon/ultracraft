package com.ultreon.craft.client.platform;

public enum OperatingSystem {
    ANDROID(GdxPlatform.ANDROID, PlatformType.MOBILE),
    IOS(GdxPlatform.IOS, PlatformType.MOBILE),
    WINDOWS(GdxPlatform.DESKTOP, PlatformType.DESKTOP),
    LINUX(GdxPlatform.DESKTOP, PlatformType.DESKTOP),
    SOLARIS(GdxPlatform.DESKTOP, PlatformType.DESKTOP),
    OS2(GdxPlatform.DESKTOP, PlatformType.DESKTOP),
    UNIX(GdxPlatform.DESKTOP, PlatformType.DESKTOP),
    MAC_OS(GdxPlatform.DESKTOP, PlatformType.DESKTOP),
    WEB(GdxPlatform.WEB, PlatformType.WEB);

    private final GdxPlatform gdxPlatform;
    private final PlatformType type;

    OperatingSystem(GdxPlatform gdxPlatform, PlatformType type) {
        this.gdxPlatform = gdxPlatform;
        this.type = type;
    }

    public GdxPlatform getGdxPlatform() {
        return this.gdxPlatform;
    }

    public PlatformType getType() {
        return this.type;
    }
}
