package com.ultreon.craft.world;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;

@SuppressWarnings("ClassCanBeRecord")
public class SoundEvent {
    private final float range;

    public SoundEvent(float range) {
        this.range = range;
    }

    public ElementID getId() {
        return Registries.SOUND_EVENT.getKey(this);
    }

    public float getRange() {
        return this.range;
    }
}
