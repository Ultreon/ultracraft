package com.ultreon.craft.ubo;

import com.ultreon.data.types.IType;

/**
 * Interface for data holders.
 *
 * @param <T> The type of the UBO data.
 */
public interface DataHolder<T extends IType<?>> extends DataWriter<T>, DataReader<T> {

}
