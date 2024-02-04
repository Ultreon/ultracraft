package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidCoordinateError extends InvalidValueError {

    public InvalidCoordinateError(String which) {
        super(which + "-coord");
    }

    public InvalidCoordinateError(String which, int index) {
        super(which + "-coord", index);
    }

    public InvalidCoordinateError(String which, String got) {
        super(which + "-coord", got);
    }

    public InvalidCoordinateError(String which, String got, int index) {
        super(which + "-coord", got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}