package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidLongError extends InvalidValueError {
    private static final String WHAT = "64-bit integer";

    public InvalidLongError() {
        super("64-bit integer");
    }

    public InvalidLongError(int index) {
        super("64-bit integer", index);
    }

    public InvalidLongError(String got) {
        super(InvalidLongError.WHAT, got);
    }

    public InvalidLongError(String got, int index) {
        super(InvalidLongError.WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}