package com.ultreon.craft.world;

/**
 * Function that creates an object with a seed.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @param <T> the type of the object
 */
@FunctionalInterface
public interface SeededSupplier<T> {
    /**
     * Returns an object with the specified seed.
     * 
     * @param seed the seed to use
     * @return     the object with the seed
     */
    T get(long seed);
}
