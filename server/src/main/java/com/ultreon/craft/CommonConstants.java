package com.ultreon.craft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonConstants {
    public static final String EX_NOT_ON_RENDER_THREAD = "Current thread is not the rendering thread.";
    public static final String EX_FAILED_TO_LOAD_CONFIG = "Failed to load config file!";
    public static final String EX_FAILED_TO_SEND_PACKET = "Failed to send packet:";
    public static final String EX_INVALID_DATA = "Invalid data";
    public static final String EX_ARRAY_TOO_LARGE = "Array too large, max = %d, actual = %d";
    public static final String NAMESPACE = "ultracraft";

    private CommonConstants() {

    }

    public static final Logger LOGGER = LoggerFactory.getLogger("Ultracraft");
}
