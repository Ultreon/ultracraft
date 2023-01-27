package com.ultreon.craft.event;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

public final class EventResult<T> {
    private final boolean interrupted;
    private final T value;

    public EventResult(boolean interrupt, @Nullable T value) {
        this.interrupted = interrupt;
        this.value = value;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    public static <T> EventResult<T> pass() {
        return new EventResult<>(false, null);
    }

    public static EventResult<Void> stop() {
        return new EventResult<>(true, null);
    }

    public static <T> EventResult<T> stop(T value) {
        Preconditions.checkNotNull(value, "Expected non-null value. Use stop() or pass() to use an empty value.");
        return new EventResult<>(true, value);
    }
}
