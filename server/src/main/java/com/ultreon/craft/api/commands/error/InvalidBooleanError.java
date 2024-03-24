package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidBooleanError extends InvalidValueError {
    private static final String WHAT = "boolean";

    public InvalidBooleanError() {
        super(InvalidBooleanError.WHAT);
    }

    public InvalidBooleanError(int index) {
        super(InvalidBooleanError.WHAT, index);
    }

    public InvalidBooleanError(String got) {
        super(InvalidBooleanError.WHAT, got);
    }

    public InvalidBooleanError(String got, int index) {
        super(InvalidBooleanError.WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}