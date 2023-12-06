package com.ultreon.craft.api.commands.error;

public class InvalidLocationError extends InvalidValueError {
    public static final String WHAT = "location";

    public InvalidLocationError() {
        super(InvalidLocationError.WHAT);
    }

    public InvalidLocationError(int index) {
        super(InvalidLocationError.WHAT, index);
    }

    public InvalidLocationError(String got) {
        super(InvalidLocationError.WHAT, got);
    }

    public InvalidLocationError(String got, int index) {
        super(InvalidLocationError.WHAT, got, index);
    }

    @Override
    public String getName() {
        return "Invalid";
    }
}