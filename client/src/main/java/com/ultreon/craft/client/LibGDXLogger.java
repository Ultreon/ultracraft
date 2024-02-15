package com.ultreon.craft.client;

import com.badlogic.gdx.ApplicationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

class LibGDXLogger implements ApplicationLogger {
    private final Logger LOGGER = LoggerFactory.getLogger("LibGDX");

    @Override
    public void log(String tag, String message) {
        this.LOGGER.info(MarkerFactory.getMarker(tag), message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        this.LOGGER.info(MarkerFactory.getMarker(tag), message, exception);
    }

    @Override
    public void error(String tag, String message) {
        this.LOGGER.error(MarkerFactory.getMarker(tag), message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        this.LOGGER.error(MarkerFactory.getMarker(tag), message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        this.LOGGER.debug(MarkerFactory.getMarker(tag), message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        this.LOGGER.debug(MarkerFactory.getMarker(tag), message, exception);
    }
}
