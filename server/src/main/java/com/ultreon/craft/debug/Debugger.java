package com.ultreon.craft.debug;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debugger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Debugger.class);

    public static void log(String message) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Debugger.LOGGER.debug(message);
        }
    }
    public static void log(String message, Throwable t) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Debugger.LOGGER.debug(message, t);
        }
    }
}
