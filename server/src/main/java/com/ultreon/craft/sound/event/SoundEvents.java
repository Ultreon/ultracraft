package com.ultreon.craft.sound.event;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.libs.commons.v0.Identifier;

public class SoundEvents {
    public static final SoundEvent PLAYER_HURT = SoundEvents.register("entity.player.hurt", new SoundEvent(10.0f));

    private static SoundEvent register(String name, SoundEvent event) {
        Registries.SOUND_EVENTS.register(new Identifier(name), event);
        return event;
    }

    public static void nopInit() {

    }
}
