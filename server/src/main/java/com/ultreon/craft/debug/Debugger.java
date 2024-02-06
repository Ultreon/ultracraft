package com.ultreon.craft.debug;

import com.ultreon.craft.GamePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debugger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Debugger.class);

    /**
     * Only logs debug messages when Fabric is in development environment.
     *
     * @param message the debug message.
     * @see GamePlatform#isDevEnvironment()
     */
    public static void log(String message) {
        if (GamePlatform.get().isDevEnvironment() || DebugFlags.IS_RUNNING_IN_DEBUG) {
            Debugger.LOGGER.debug(message);
        }
    }

    /**
     * Only logs debug messages when Fabric is in development environment.
     *
     * @param message the debug message.
     * @param t       the exception.
     */
    public static void log(String message, Throwable t) {
        if (GamePlatform.get().isDevEnvironment() || DebugFlags.IS_RUNNING_IN_DEBUG) {
            Debugger.LOGGER.debug(message, t);
        }
    }
}
