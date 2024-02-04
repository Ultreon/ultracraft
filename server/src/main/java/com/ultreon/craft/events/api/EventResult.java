package com.ultreon.craft.events.api;

public final class EventResult {
    private final boolean interrupted;
    private final boolean canceled;

    public EventResult(boolean interrupt, boolean canceled) {
        this.interrupted = interrupt;
        this.canceled = canceled;
    }

    public boolean isInterrupted() {
        return this.interrupted;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public static EventResult pass() {
        return new EventResult(false, false);
    }

    public static EventResult interruptCancel() {
        return new EventResult(true, true);
    }

    public static EventResult interrupt() {
        return new EventResult(true, false);
    }

    public static EventResult interrupt(boolean cancel) {
        return new EventResult(true, cancel);
    }
}
