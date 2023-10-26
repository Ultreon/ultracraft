package com.ultreon.craft.events;

import com.ultreon.craft.world.BlockPos;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.World;
import com.ultreon.libs.events.v1.Event;

public class BlockEvents {
    public static final Event<SetBlock> SET_BLOCK = Event.create();

    @FunctionalInterface
    public interface SetBlock {
        void onSetBlock(World world, BlockPos pos, Block block);
    }
}
