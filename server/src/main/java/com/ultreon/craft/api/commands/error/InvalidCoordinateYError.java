package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidCoordinateYError extends InvalidCoordinateError {

    public InvalidCoordinateYError() {
        super("y");
    }

    public InvalidCoordinateYError(int index) {
        super("y", index);
    }

    public InvalidCoordinateYError(String which, String got) {
        super("y", got);
    }

    public InvalidCoordinateYError(String which, String got, int index) {
        super("y", got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}