package com.ultreon.craft.api.commands.error;

public class TargetNotFoundError extends NotFoundError {

    public TargetNotFoundError(String name) {
        super("target " + name);
    }

    public TargetNotFoundError(String name, int index) {
        super("target " + name, index);
    }

    @Override
    public String getName() {
        return "NotFound";
    }
}