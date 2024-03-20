package com.ultreon.gameprovider.craft;

import com.badlogic.gdx.utils.SharedLibraryLoader;

/**
 * Class used to check for which operating system the game is running on.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class OS {
    /**
     * Checks if the operating system is Windows.
     *
     * @return true if the operating system is Windows, false otherwise
     */
    public static boolean isWindows() {
        return OS.getOSType() == OSType.Windows;
    }

    /**
     * Checks if the operating system is macOS.
     *
     * @return true if the operating system is macOS, false otherwise
     */
    public static boolean isMac() {
        return OS.getOSType() == OSType.Mac;
    }

    /**
     * Checks if the operating system is Linux.
     *
     * @return true if the operating system is Linux, false otherwise
     */
    public static boolean isLinux() {
        return OS.getOSType() == OSType.Linux;
    }

    /**
     * Checks if the operating system is Android.
     *
     * @return true if the operating system is Android, false otherwise
     */
    public static boolean isAndroid() {
        return OS.getOSType() == OSType.Android;
    }

    /**
     * Checks if the operating system is iOS.
     *
     * @return true if the operating system is iOS, false otherwise
     */
    public static boolean isIos() {
        return OS.getOSType() == OSType.Ios;
    }

    /**
     * Returns the operating system type.
     *
     * @return the operating system type
     */
    private static OSType getOSType() {
        // Check if the code is running on Android
        if (SharedLibraryLoader.isAndroid) {
            return OSType.Android;
        }

        // Check if the code is running on iOS
        if (SharedLibraryLoader.isIos) {
            return OSType.Linux;
        }

        // Check if the code is running on Windows
        if (SharedLibraryLoader.isWindows) {
            return OSType.Windows;
        }

        // Check if the code is running on Mac
        if (SharedLibraryLoader.isMac) {
            return OSType.Mac;
        }

        // Check if the code is running on Linux
        if (SharedLibraryLoader.isLinux) {
            return OSType.Linux;
        }

        // Return Unknown if the operating system type cannot be determined
        return OSType.Unknown;
    }

    /**
     * Checks if the current operating system is mobile.
     *
     * @return true if the operating system is mobile, false otherwise
     */
    public static boolean isMobile() {
        return OS.getOSType().isMobile();
    }

    /**
     * Enum representing different Operating System types.
     */
    private enum OSType {
        Linux(false), // Represents the Linux Operating System
        Windows(false), // Represents the Windows Operating System
        Mac(false), // Represents the Mac Operating System
        Android(true), // Represents the Android Operating System
        Ios(true), // Represents the iOS Operating System
        Unknown(false); // Represents an unknown Operating System

        private final boolean isMobile;

        /**
         * Constructor for OSType enum.
         *
         * @param isMobile Boolean indicating if the OS is mobile.
         */
        OSType(boolean isMobile) {
            this.isMobile = isMobile;
        }

        /**
         * Check if the OS is mobile.
         *
         * @return True if the OS is mobile, false otherwise.
         */
        public boolean isMobile() {
            return this.isMobile;
        }
    }
}
