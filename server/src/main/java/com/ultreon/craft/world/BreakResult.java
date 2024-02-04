package com.ultreon.craft.world;

/**
 * Enum representing the result of a block breaking operation.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public enum BreakResult {
    /**
     * Indicates that the block has been broken.
     */
    BROKEN,

    /**
     * Indicates that the block can still continue to break.
     */
    CONTINUE,

    /**
     * Indicates that the block either doesn't exist or an event cancelled the block breaking.
     */
    FAILED
}
