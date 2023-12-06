package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidFloatError extends InvalidValueError {
    private static final String WHAT = "float";

    public InvalidFloatError() {
        super(InvalidFloatError.WHAT);
    }

    public InvalidFloatError(int index) {
        super(InvalidFloatError.WHAT, index);
    }

    public InvalidFloatError(String got) {
        super(InvalidFloatError.WHAT, got);
    }

    public InvalidFloatError(String got, int index) {
        super(InvalidFloatError.WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}