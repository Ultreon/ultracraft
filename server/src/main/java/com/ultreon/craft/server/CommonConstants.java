package com.ultreon.craft.server;

import java.util.concurrent.TimeUnit;

public class CommonConstants {
    public static final String NAMESPACE = "ultracraft"; //? Should we use "ultracraft" instead?
    public static final TimeUnit AUTO_SAVE_DELAY_UNIT = TimeUnit.SECONDS;
    public static final int AUTO_SAVE_DELAY = 60;
    public static final int INITIAL_AUTO_SAVE_DELAY = 120;
    public static final boolean INSPECTION_ENABLED = true; //! Only enable for debugging
}