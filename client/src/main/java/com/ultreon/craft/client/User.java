package com.ultreon.craft.client;

/**
 * Represents a user.
 */
public record User(String name) {
    /**
     * Returns a string representation of the user.
     *
     * @return the name of the user
     */
    @Override
    public String toString() {
        return name;
    }
}
