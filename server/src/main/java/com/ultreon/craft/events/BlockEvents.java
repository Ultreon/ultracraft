package com.ultreon.craft.events;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;

public class BlockEvents {
    public static final Event<SetBlock> SET_BLOCK = Event.create();

    @FunctionalInterface
    public interface SetBlock {
        void onSetBlock(World world, BlockPos pos, Block block);
    }
}
