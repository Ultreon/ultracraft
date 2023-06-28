package com.ultreon.craft.debug;

import com.ultreon.craft.GameFlags;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static com.ultreon.craft.UltreonCraft.LOGGER;

public class Debugger {
    private static final Marker MARKER = MarkerFactory.getMarker("Debugger");

    public static void log(String message) {
        if (GameFlags.DEBUG) {
            LOGGER.debug(MARKER, message);
        }
    }
}
