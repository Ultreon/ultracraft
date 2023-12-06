package com.ultreon.craft.gamerule;

import com.ultreon.craft.api.commands.CommandExecuteException;

public interface Rule<T> {
    String getKey();
    void setValue(T value);
    T getValue();
    T getDefault();
    String getStringValue();
    void setStringValue(String value) throws CommandExecuteException;

    default void reset() {
        this.setValue(this.getDefault());
    }
}