package com.ultreon.craft.experiments;

public class ExperimentalFeature {
    private Boolean defaultState;

    public ExperimentalFeature() {
        this.defaultState = false;
    }

    public ExperimentalFeature(boolean enabledByDefault) {
        this.defaultState = enabledByDefault;
    }

    public void toggle() {
        ExperimentalFeatures.toggle(this);
    }

    public void enable() {
        ExperimentalFeatures.setEnabled(this, true);
    }

    public void disable() {
        ExperimentalFeatures.setEnabled(this, false);
    }

    public boolean isEnabled() {
        return ExperimentalFeatures.isEnabled(this);
    }

    public void update() {
        // Implement me
    }

    public boolean getDefaultState() {
        return defaultState;
    }
}
