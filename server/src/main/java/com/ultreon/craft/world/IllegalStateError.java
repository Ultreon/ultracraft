package com.ultreon.craft.world;

/**
 * An error that is thrown when the world, region or chunk is in an illegal state.
 * This is a hard error, which would mean the game should crash.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public class IllegalStateError extends Error {
    public IllegalStateError(String message) {
        super(message);
    }

    public IllegalStateError(String message, InterruptedException e) {
        super(message, e);
    }
}
