package com.ultreon.craft.ubo;

import com.ultreon.data.types.IType;

/**
 * Interface for data writers.
 *
 * @param <T>
 */
@FunctionalInterface
public interface DataWriter<T extends IType<?>> {
    /**
     * Saves this object to a UBO object.
     *
     * @return the UBO object
     */
    T save();
}
