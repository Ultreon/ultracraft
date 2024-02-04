package com.ultreon.craft.api.commands.error;

public class InvalidCoordinateXError extends InvalidCoordinateError {
    private String name = "Invalid";

    public InvalidCoordinateXError() {
        super("x");
    }

    public InvalidCoordinateXError(int index) {
        super("x", index);
    }

    public InvalidCoordinateXError(String got) {
        super("x", got);
    }

    public InvalidCoordinateXError(String got, int index) {
        super("x", got, index);
    }

    @Override
    public String getName() {
        return this.name;
    }
}