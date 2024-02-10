package com.ultreon.craft.client.input;

public interface BooleanInputDefinition extends InputDefinition<Boolean> {
    @Override
    @Deprecated
    default Boolean getValue() {
        return getBooleanValue();
    }

    boolean getBooleanValue();
}
