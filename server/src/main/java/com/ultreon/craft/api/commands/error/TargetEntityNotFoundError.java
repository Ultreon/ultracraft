package com.ultreon.craft.api.commands.error;

import org.jetbrains.annotations.NotNull;

public class TargetEntityNotFoundError extends TargetNotFoundError {
    private final String name = "NotFound";

    public TargetEntityNotFoundError(String name) {
        super(name + " Entity");
    }

    public TargetEntityNotFoundError(String name, int index) {
        super(name + " Entity", index);
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }
}