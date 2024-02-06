package com.ultreon.craft.debug;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.GamePlatform;

import java.lang.management.ManagementFactory;
import java.util.List;

public class DebugFlags {
    private static boolean detectDebug() {
        return GamePlatform.get().detectDebug();
    }


    public static final boolean IS_RUNNING_IN_DEBUG = detectDebug();

    public static final DebugFlag CHUNK_PACKET_DUMP = new DebugFlag(false);
    public static final DebugFlag CHUNK_BLOCK_DATA_DUMP = new DebugFlag(false);
    public static final DebugFlag WARN_CHUNK_BUILD_OVERLOAD = new DebugFlag(true);
    public static final DebugFlag INSPECTION_ENABLED = new DebugFlag(true); //! Only enable for debugging
    public static final DebugFlag DUMP_TEXTURE_ATLAS = new DebugFlag(true);
    public static final DebugFlag WORLD_GEN = new DebugFlag(false);
    public static final DebugFlag LOG_POSITION_RESET_ON_CHUNK_LOAD = new DebugFlag(false);

    public static final boolean SENSOR_DEBUG = false; // TODO Update to DebugFlag
    public static final boolean DUMP_REGISTRIES = true; // TODO Update to DebugFlag

    static {
        if (IS_RUNNING_IN_DEBUG)
            CommonConstants.LOGGER.warn("Running in debug mode.");
    }
}
