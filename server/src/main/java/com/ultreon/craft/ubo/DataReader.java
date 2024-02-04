package com.ultreon.craft.ubo;

import com.ultreon.data.types.IType;

/**
 * Interface for data readers.
 *
 * @param <T>
 */
public interface DataReader<T extends IType<?>> {
    /**
     * Loads this object from a UBO object.
     *
     * @param data the UBO object
     */
    void load(T data);
}
