package com.ultreon.craft.debug;

import com.ultreon.craft.CommonConstants;

import java.lang.management.ManagementFactory;
import java.util.List;

public class DebugFlags {
    private static boolean detectDebug() {
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        boolean debugFlagPresent = args.contains("-Xdebug");
        boolean jdwpPresent = args.toString().contains("jdwp");
        return debugFlagPresent || jdwpPresent;
    }


    public static final boolean IS_RUNNING_IN_DEBUG = detectDebug();

    public static final DebugFlag CHUNK_PACKET_DUMP = new DebugFlag(false);
    public static final DebugFlag CHUNK_BLOCK_DATA_DUMP = new DebugFlag(false);
    public static final DebugFlag WARN_CHUNK_BUILD_OVERLOAD = new DebugFlag(true);
    public static final DebugFlag INSPECTION_ENABLED = new DebugFlag(true); //! Only enable for debugging
    public static final DebugFlag DUMP_TEXTURE_ATLAS = new DebugFlag(true);
    public static final DebugFlag WORLD_GEN = new DebugFlag(false);
    public static final DebugFlag LOG_POSITION_RESET_ON_CHUNK_LOAD = new DebugFlag(false);

    static {
        if (IS_RUNNING_IN_DEBUG)
            CommonConstants.LOGGER.warn("Running in debug mode.");
    }
}
