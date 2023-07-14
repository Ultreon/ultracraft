package com.ultreon.craft.world;

import com.ultreon.data.types.MapType;

import java.time.Instant;

public class WorldInfo {
    private final String name;
    private final Instant lastPlayed;
    private final long seed;

    public WorldInfo(String name, Instant lastPlayed, long seed) {
        this.name = name;
        this.lastPlayed = lastPlayed;
        this.seed = seed;
    }

    public WorldInfo(MapType data) {
        this.name = data.getString("worldName");
        this.lastPlayed = Instant.ofEpochMilli(data.getLong("lastPlayed"));
        this.seed = data.getLong("seed");
    }

    public String getName() {
        return this.name;
    }

    public Instant getLastPlayed() {
        return this.lastPlayed;
    }

    public long getSeed() {
        return this.seed;
    }

    public MapType save() {
        MapType data = new MapType();
        data.putString("worldName", this.name);
        data.putLong("lastPlayed", this.lastPlayed.toEpochMilli());
        return data;
    }
}
