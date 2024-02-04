package com.ultreon.craft.api.commands.error;

public class InvalidKeyError extends InvalidValueError {
    private static final String WHAT = "key";

    public InvalidKeyError() {
        super(InvalidKeyError.WHAT);
    }

    public InvalidKeyError(int index) {
        super(InvalidKeyError.WHAT, index);
    }

    public InvalidKeyError(String got) {
        super(InvalidKeyError.WHAT, got);
    }

    public InvalidKeyError(String got, int index) {
        super(InvalidKeyError.WHAT, got, index);
    }

    @Override
    public String getName() {
        return "Invalid";
    }
}