package com.ultreon.craft.util;

public enum Gamemode {
    SURVIVAL, MINI_GAME, BUILDER, BUILDER_PLUS;

    public static Gamemode byOrdinal(int ordinal) {
        Gamemode[] values = Gamemode.values();
        if (ordinal >= values.length || ordinal < 0) {
            return null;
        }
        return values[ordinal];
    }
}
