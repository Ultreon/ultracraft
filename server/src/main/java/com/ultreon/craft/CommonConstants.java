package com.ultreon.craft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonConstants {
    public static final String EX_NOT_ON_RENDER_THREAD = "Current thread is not the rendering thread.";

    private CommonConstants() {

    }

    public static final Logger LOGGER = LoggerFactory.getLogger("Ultracraft");
}
