package com.ultreon.craft.client.platform;

public enum GdxPlatform {
    ANDROID("Android"),IOS("iOS"),DESKTOP("Desktop"),WEB("Web");

    private final String displayName;

    GdxPlatform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
