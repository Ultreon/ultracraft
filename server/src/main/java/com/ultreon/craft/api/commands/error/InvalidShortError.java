package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidShortError extends InvalidValueError {
    private static final String WHAT = "16-bit integer";

    public InvalidShortError() {
        super("16-bit integer");
    }

    public InvalidShortError(int index) {
        super("16-bit integer", index);
    }

    public InvalidShortError(String got) {
        super(InvalidShortError.WHAT, got);
    }

    public InvalidShortError(String got, int index) {
        super(InvalidShortError.WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}