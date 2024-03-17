package com.ultreon.craft.util;

import com.ultreon.craft.entity.player.PlayerAbilities;

public enum Gamemode {
    SURVIVAL(false, false, false, true),
    MINI_GAME(false, false, false, false),
    BUILDER(true, true, true, true),
    BUILDER_PLUS(true, true, true, true),
    SPECTATOR(true, false, true, false);

    private final boolean allowFlight;
    private final boolean instaMine;
    private final boolean invincible;
    private final boolean blockBreak;

    Gamemode(boolean allowFlight, boolean instaMine, boolean invincible, boolean blockBreak) {
        this.allowFlight = allowFlight;
        this.instaMine = instaMine;
        this.invincible = invincible;
        this.blockBreak = blockBreak;
    }

    public PlayerAbilities setAbilities(PlayerAbilities abilities) {
        abilities.allowFlight = this.allowFlight;
        abilities.instaMine = this.instaMine;
        abilities.invincible = this.invincible;
        abilities.blockBreak = this.blockBreak;
        return abilities;
    }

    public static Gamemode byOrdinal(int ordinal) {
        Gamemode[] values = Gamemode.values();
        if (ordinal >= values.length || ordinal < 0) {
            return null;
        }
        return values[ordinal];
    }
}
