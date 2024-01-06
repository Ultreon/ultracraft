package com.ultreon.craft.crash;

import com.ultreon.craft.CommonConstants;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception that is thrown when an application crash occurs
 *
 * @author <a href="https://github.com/XyperCodee">XyperCode</a>
 * @see ApplicationCrash
 * @since 0.1.0
 */
public final class ApplicationCrash extends Error {
    private static final List<Runnable> crashHandlers = new ArrayList<>();
    @NotNull
    private final CrashLog crashLog;

    public ApplicationCrash(@NotNull CrashLog crashLog) {
        this.crashLog = crashLog;
    }

    public void printCrash() {
        String crashString = this.crashLog.toString();
        String[] strings = crashString.split("(\r\n|\r|\n)");
        for (String string : strings) {
            CommonConstants.LOGGER.error(string);
        }
    }

    @Override
    public String toString() {
        return crashLog.toString();
    }

    @ApiStatus.Internal
    public void handleCrash() {
        for (Runnable handler : crashHandlers) {
            handler.run();
        }
    }

    public static void onCrash(Runnable handler) {
        crashHandlers.add(handler);
    }

    @NotNull
    public CrashLog getCrashLog() {
        return this.crashLog;
    }
}
