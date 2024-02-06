package com.ultreon.craft.world;

/**
 * Enum representing the result of a player interaction.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public enum InteractResult {
    /**
     * Indicates that the interaction was allowed.
     */
    ALLOW,

    /**
     * Indicates that the interaction was skipped.
     */
    SKIP,

    /**
     * Indicates that the interaction was denied.
     */
    DENY
}
