package com.ultreon.bubbles.gradle

enum class Platform {
    COMMON, WINDOWS, MAC, LINUX;

    companion object {
        @JvmStatic
        fun get(config: String?): Platform? {
            return when (config) {
                "win32" -> WINDOWS
                "linux" -> LINUX
                "mac" -> MAC
                "common" -> COMMON
                else -> null
            }
        }
    }
}