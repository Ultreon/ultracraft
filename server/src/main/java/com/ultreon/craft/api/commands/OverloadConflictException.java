package com.ultreon.craft.api.commands;

public class OverloadConflictException extends RuntimeException {
    private final String[] aliases;

    public OverloadConflictException(String... aliases) {
        this.aliases = aliases;
    }

    public String[] getAliases() {
        return this.aliases;
    }
}
