package com.ultreon.craft.init;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class Sounds {
    public static final SoundEvent PlAYER_HURT = register("player_hurt", new SoundEvent());

    private static SoundEvent register(String name, SoundEvent sound) {
        Registries.SOUNDS.register(new Identifier(UltreonCraft.NAMESPACE, name), sound);
        return sound;
    }

    public static void nopInit() {

    }
}
