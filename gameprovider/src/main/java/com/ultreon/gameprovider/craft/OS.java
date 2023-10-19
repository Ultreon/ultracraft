package com.ultreon.gameprovider.craft;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class OS {
    public static boolean isWindows() {
        return OS.getOSType() == OSType.Windows;
    }

    public static boolean isMac() {
        return OS.getOSType() == OSType.Mac;
    }

    public static boolean isLinux() {
        return OS.getOSType() == OSType.Linux;
    }

    public static boolean isAndroid() {
        return OS.getOSType() == OSType.Android;
    }

    public static boolean isIos() {
        return OS.getOSType() == OSType.Ios;
    }

    private static OSType getOSType() {
        if (SharedLibraryLoader.isAndroid) return OSType.Android;
        if (SharedLibraryLoader.isIos) return OSType.Linux;
        if (SharedLibraryLoader.isWindows) return OSType.Windows;
        if (SharedLibraryLoader.isMac) return OSType.Mac;
        if (SharedLibraryLoader.isLinux) return OSType.Linux;
        return OSType.Unknown;
    }

    public static boolean isMobile() {
        return OS.getOSType().isMobile();
    }

    private enum OSType {
        Linux(false),
        Windows(false),
        Mac(false),
        Android(true),
        Ios(true),
        Unknown(false);

        private final boolean isMobile;

        OSType(boolean isMobile) {
            this.isMobile = isMobile;
        }

        public boolean isMobile() {
            return this.isMobile;
        }
    }
}
