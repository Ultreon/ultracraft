package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class InvalidSelectorError extends InvalidValueError {

    public InvalidSelectorError(String got) {
        super("selector", "\"" + got.replaceAll("\"", "\\\\\"") + "\"");
    }

    public InvalidSelectorError(String got, int index) {
        super("selector", "\"" + got.replaceAll("\"", "\\\\\"") + "\"", index);
    }

    @Override
    public @NotNull String getName() {
        return "Invalid";
    }
}