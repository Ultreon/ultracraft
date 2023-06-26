package com.ultreon.craft.debug;

import com.ultreon.craft.GameFlags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debugger {
    private static final Logger LOGGER = LoggerFactory.getLogger("Debugger");

    public static void log(String message) {
        if (GameFlags.DEBUG) {
            LOGGER.debug(message);
        }
    }
}
