package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidByteError extends InvalidValueError {
    private static final String WHAT = "byte";

    public InvalidByteError() {
        super(WHAT);
    }

    public InvalidByteError(int index) {
        super(WHAT, index);
    }

    public InvalidByteError(String got) {
        super(WHAT, got);
    }

    public InvalidByteError(String got, int index) {
        super(WHAT, got, index);
    }

    public @NotNull String getName() {
        return "Invalid";
    }
}