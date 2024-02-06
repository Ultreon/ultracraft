package com.ultreon.craft.debug;

public record DebugFlag(boolean enabled) {
    @Override
    public boolean enabled() {
        return enabled && DebugFlags.IS_RUNNING_IN_DEBUG;
    }
}
