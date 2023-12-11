package com.ultreon.craft.entity.player;

import com.ultreon.data.types.MapType;

public class PlayerAbilities {
    public boolean flying = false;
    public boolean allowFlight = false;
    public boolean instaMine = false;
    public boolean invincible = false;
    public boolean blockBreak = true;

    public void load(MapType data) {
        this.flying = data.getBoolean("flying");
        this.allowFlight = data.getBoolean("allowFlight");
    }

    public MapType save(MapType data) {
        data.putBoolean("flying", this.flying);
        data.putBoolean("allowFlight", this.allowFlight);
        return data;
    }
}
