package com.ultreon.craft.client.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.SoundEvent;

public final class ClientSound {
    private final SoundEvent event;
    private Sound sound;

    public ClientSound(SoundEvent event) {
        this.event = event;
    }

    public void register() {
        this.sound = Gdx.audio.newSound(Gdx.files.internal(String.format("assets/%s/sounds/%s.mp3", this.getId().namespace(), this.getId().path().replace(".", "/"))));
    }

    public Identifier getId() {
        return Registries.SOUND_EVENT.getId(this.event);
    }

    public Sound getSound() {
        return this.sound;
    }
}
