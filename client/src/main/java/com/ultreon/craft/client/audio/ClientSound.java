package com.ultreon.craft.client.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.libs.commons.v0.Identifier;

public final class ClientSound {
    private final SoundEvent event;
    private Sound sound;

    public ClientSound(SoundEvent event) {
        this.event = event;
    }

    public void register() {
        this.sound = Gdx.audio.newSound(Gdx.files.internal(String.format("assets/%s/sounds/%s.mp3", this.getId().location(), this.getId().path())));
    }

    public Identifier getId() {
        return Registries.SOUND_EVENTS.getKey(this.event);
    }

    public Sound getSound() {
        return this.sound;
    }
}
