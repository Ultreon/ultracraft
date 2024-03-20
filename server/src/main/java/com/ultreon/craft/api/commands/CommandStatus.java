package com.ultreon.craft.api.commands;

/**
 * Enum representing the status of a command.
 */
public enum CommandStatus {
    WIP("Work in progress"),
    DONE("Completed"),
    OUTDATED("Obsolete"),
    DEPRECATED("No longer supported"),
    DEBUG("For debugging purposes");

    private final String description;

    CommandStatus(String description) {
        this.description = description;
    }

    /**
     * Get the description of the command status.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }
}