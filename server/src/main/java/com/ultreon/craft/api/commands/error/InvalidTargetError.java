package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidTargetError extends InvalidValueError {
    private static final String WHAT = "target";

    public InvalidTargetError() {
        super("target");
    }

    public InvalidTargetError(int index) {
        super("target", index);
    }

    public InvalidTargetError(String got) {
        super(InvalidTargetError.WHAT, got);
    }

    public InvalidTargetError(String got, int index) {
        super(InvalidTargetError.WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}