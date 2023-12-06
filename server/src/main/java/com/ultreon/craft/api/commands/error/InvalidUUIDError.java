package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidUUIDError extends InvalidValueError {
    private static final String WHAT = "uuid";

    public InvalidUUIDError() {
        super("uuid");
    }

    public InvalidUUIDError(int index) {
        super("uuid", index);
    }

    public InvalidUUIDError(String got) {
        super(WHAT, got);
    }

    public InvalidUUIDError(String got, int index) {
        super(WHAT, got, index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}