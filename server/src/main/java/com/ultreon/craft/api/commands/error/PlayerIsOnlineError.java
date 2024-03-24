package com.ultreon.craft.api.commands.error;

public class PlayerIsOnlineError extends CommandError {
    private final String name;

    public PlayerIsOnlineError(String name) {
        super("Player " + name + " is online.");
        this.name = "Generic";
    }

    @Override
    public String getName() {
        return this.name;
    }
}