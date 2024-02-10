package com.ultreon.craft.client.input;

public interface FloatInputDefinition extends InputDefinition<Float> {
    @Override
    @Deprecated
    default Float getValue() {
        return getFloatValue();
    }

    float getFloatValue();
}
