package com.ultreon.craft.client;

/**
 * Represents a user in the game.
 *
 * @since 0.1.0
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @param name the name of the user
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
