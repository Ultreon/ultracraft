package com.ultreon.craft.weather;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public class Weather {
    public static final Weather SUNNY = new Weather();
    public static final Weather RAIN = new Weather();
    public static final Weather THUNDER = new Weather();

    public @Nullable Identifier getId() {
        return Registries.WEATHER.getId(this);
    }
}
