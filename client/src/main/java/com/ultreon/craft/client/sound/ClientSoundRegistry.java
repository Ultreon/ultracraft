package com.ultreon.craft.client.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.SoundEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientSoundRegistry {
    private Map<ElementID, Sound> soundMap = Collections.emptyMap();

    public ClientSoundRegistry() {

    }

    @ApiStatus.Internal
    public void registerSounds() {
        Registry<SoundEvent> soundEvents = Registries.SOUND_EVENT;
        Map<ElementID, Sound> soundMap = new HashMap<>();
        for (Map.Entry<ElementID, SoundEvent> entry : soundEvents.entries()) {
            ElementID key = entry.getKey();
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(String.format("assets/%s/sounds/%s.mp3", key.namespace(), key.path().replaceAll("\\.", "/"))));

            soundMap.put(key, sound);
        }

        this.soundMap = soundMap;
    }

    public Sound getSound(ElementID id) {
        return this.soundMap.get(id);
    }
}
