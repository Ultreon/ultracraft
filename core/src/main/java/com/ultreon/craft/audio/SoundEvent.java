package com.ultreon.craft.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.resources.ResourceFileHandle;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.Objects;

public final class SoundEvent {
    private Identifier id;
    private Sound sound;

    public void register() {
        this.id = Objects.requireNonNull(Registries.SOUNDS.getKey(this), "Sound not registered: " + getClass().getName());
        this.sound = Gdx.audio.newSound(new ResourceFileHandle(this.id.mapPath("sounds/%s.mp3"::formatted)));
    }

    public Identifier getId() {
        return id;
    }

    public Sound getSound() {
        return sound;
    }
}
