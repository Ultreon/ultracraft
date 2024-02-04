package com.ultreon.craft.api.commands.error;

public class InvalidIntegerError extends InvalidValueError {
    private static final String WHAT = "integer";

    public InvalidIntegerError() {
        super(InvalidIntegerError.WHAT);
    }

    public InvalidIntegerError(int index) {
        super(InvalidIntegerError.WHAT, index);
    }

    public InvalidIntegerError(String got) {
        super(InvalidIntegerError.WHAT, got);
    }

    public InvalidIntegerError(String got, int index) {
        super(InvalidIntegerError.WHAT, got, index);
    }

    @Override
    public String getName() {
        return "Invalid";
    }
}