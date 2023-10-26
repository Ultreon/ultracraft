package com.ultreon.craft.ubo;

import com.ultreon.data.types.IType;

public interface DataHolder<T extends IType<?>> extends DataWriter<T>, DataReader<T> {

}
