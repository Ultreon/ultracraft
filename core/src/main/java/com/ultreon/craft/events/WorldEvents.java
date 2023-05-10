package com.ultreon.craft.events;

import com.ultreon.craft.world.World;
import com.ultreon.libs.events.v1.Event;

public class WorldEvents {
    public static final Event<PreTick> PRE_TICK = Event.create();
    public static final Event<PostTick> POST_TICK = Event.create();

    @FunctionalInterface
    public interface PreTick {
        void onPreTick(World world);
    }

    @FunctionalInterface
    public interface PostTick {
        void onPostTick(World world);
    }
}
