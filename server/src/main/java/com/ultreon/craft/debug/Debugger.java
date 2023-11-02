package com.ultreon.craft.debug;

import org.quiltmc.loader.api.QuiltLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debugger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Debugger.class);

    public static void log(String message) {
        if (QuiltLoader.isDevelopmentEnvironment()) {
            Debugger.LOGGER.debug(message);
        }
    }
    public static void log(String message, Throwable t) {
        if (QuiltLoader.isDevelopmentEnvironment()) {
            Debugger.LOGGER.debug(message, t);
        }
    }
}
