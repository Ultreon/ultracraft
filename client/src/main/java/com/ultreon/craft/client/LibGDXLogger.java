package com.ultreon.craft.client;

import com.badlogic.gdx.ApplicationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

/**
 * Custom logger implementation for LibGDX.
 */
class LibGDXLogger implements ApplicationLogger {
    private final Logger LOGGER = LoggerFactory.getLogger("LibGDX");

    /**
     * Log a message with the specified tag.
     *
     * @param tag     The tag for the log message.
     * @param message The log message.
     */
    @Override
    public void log(String tag, String message) {
        this.LOGGER.info(MarkerFactory.getMarker(tag), message);
    }

    /**
     * Log a message and exception with the specified tag.
     *
     * @param tag       The tag for the log message.
     * @param message   The log message.
     * @param exception The exception to be logged.
     */
    @Override
    public void log(String tag, String message, Throwable exception) {
        this.LOGGER.info(MarkerFactory.getMarker(tag), message, exception);
    }

    /**
     * Log an error message with the specified tag.
     *
     * @param tag     The tag for the error message.
     * @param message The error message.
     */
    @Override
    public void error(String tag, String message) {
        this.LOGGER.error(MarkerFactory.getMarker(tag), message);
    }

    /**
     * Log an error message and exception with the specified tag.
     *
     * @param tag       The tag for the error message.
     * @param message   The error message.
     * @param exception The exception to be logged.
     */
    @Override
    public void error(String tag, String message, Throwable exception) {
        this.LOGGER.error(MarkerFactory.getMarker(tag), message, exception);
    }

    /**
     * Log a debug message with the specified tag.
     *
     * @param tag     The tag for the debug message.
     * @param message The debug message.
     */
    @Override
    public void debug(String tag, String message) {
        this.LOGGER.debug(MarkerFactory.getMarker(tag), message);
    }

    /**
     * Log a debug message and exception with the specified tag.
     *
     * @param tag       The tag for the debug message.
     * @param message   The debug message.
     * @param exception The exception to be logged.
     */
    @Override
    public void debug(String tag, String message, Throwable exception) {
        this.LOGGER.debug(MarkerFactory.getMarker(tag), message, exception);
    }
}