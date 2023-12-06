package com.ultreon.craft.api.commands.error;

public class TargetPlayerNotFoundError extends TargetNotFoundError {
    private final String name;

    public TargetPlayerNotFoundError() {
        super("player");
        this.name = "NotFound";
    }

    public TargetPlayerNotFoundError(int index) {
        super("player", index);
        this.name = "NotFound";
    }

    @Override
    public String getName() {
        return this.name;
    }
}