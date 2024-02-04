package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidCharError extends InvalidValueError {
    private static final String WHAT = "character";

    public InvalidCharError() {
        super(WHAT);
    }

    public InvalidCharError(int index) {
        super(WHAT, index);
    }

    public InvalidCharError(String got) {
        super(WHAT, got);
    }

    public InvalidCharError(String got, int index) {
        super(WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}