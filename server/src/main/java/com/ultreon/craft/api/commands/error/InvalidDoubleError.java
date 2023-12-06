package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidDoubleError extends InvalidValueError {
    private static final String WHAT = "double";

    public InvalidDoubleError() {
        super(InvalidDoubleError.WHAT);
    }

    public InvalidDoubleError(int index) {
        super(InvalidDoubleError.WHAT, index);
    }

    public InvalidDoubleError(String got) {
        super(InvalidDoubleError.WHAT, got);
    }

    public InvalidDoubleError(String got, int index) {
        super(InvalidDoubleError.WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}