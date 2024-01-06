package com.ultreon.craft.crash;

import org.jetbrains.annotations.NotNull;

/**
 * Exception that is thrown when an application crash occurs
 *
 * @author <a href="https://github.com/XyperCodee">XyperCode</a>
 * @see ApplicationCrash
 * @since 0.1.0
 * @deprecated Use {@link ApplicationCrash} instead
 */
@Deprecated(since="0.1.0", forRemoval = true)
public class CrashException extends RuntimeException {
    private final CrashLog crashLog;

    public CrashException(@NotNull CrashLog crashLog) {
        super("Application crashed!");
        this.crashLog = crashLog;
    }

    public CrashException(@NotNull CrashLog crashLog, @NotNull String message) {
        super(message);
        this.crashLog = crashLog;
    }

    @NotNull
    public CrashLog getCrashLog() {
        return this.crashLog;
    }
}
