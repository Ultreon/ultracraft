package com.ultreon.craft.events;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.World;
import com.ultreon.libs.events.v1.Event;

public class BlockEvents {
    public static final Event<SetBlock> SET_BLOCK = Event.create();

    @FunctionalInterface
    public interface SetBlock {
        void onSetBlock(World world, GridPoint3 pos, Block block);
    }
}
