package com.ultreon.gameprovider.craft;

import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import net.fabricmc.loader.impl.util.log.LogLevel;
import org.apache.logging.log4j.*;

import java.util.HashMap;
import java.util.Map;

public class UltreonCraftLogHandler implements LogHandler {
    private static final Logger LOGGER = LogManager.getLogger("FabricLoader");
    private final Map<LogCategory, Marker> markerMap = new HashMap<>();

    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay, boolean wasSuppressed) {
        Marker marker = this.markerMap.computeIfAbsent(category, logCategory -> MarkerManager.getMarker(logCategory.name));
        LOGGER.log(getLevel(level), marker, msg, exc);
    }

    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        return LOGGER.isEnabled(getLevel(level));
    }

    private static Level getLevel(LogLevel level) {
        switch (level) {
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case DEBUG:
                return Level.DEBUG;
            case ERROR:
                return Level.ERROR;
            case TRACE:
                return Level.TRACE;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void close() {

    }
}
