package com.ultreon.craft.client.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.registries.v0.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientSoundRegistry {
    private Map<Identifier, Sound> soundMap = Collections.emptyMap();

    public ClientSoundRegistry() {

    }

    @ApiStatus.Internal
    public void registerSounds() {
        Registry<SoundEvent> soundEvents = Registries.SOUND_EVENTS;
        Map<Identifier, Sound> soundMap = new HashMap<>();
        for (Map.Entry<Identifier, SoundEvent> entry : soundEvents.entries()) {
            Identifier key = entry.getKey();
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(String.format("assets/%s/sounds/%s.mp3", key.location(), key.path().replaceAll("\\.", "/"))));

            soundMap.put(key, sound);
        }

        this.soundMap = soundMap;
    }

    public Sound getSound(Identifier id) {
        return this.soundMap.get(id);
    }
}
