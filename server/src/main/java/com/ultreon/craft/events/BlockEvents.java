package com.ultreon.craft.events;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;

public class BlockEvents {
    public static final Event<SetBlock> SET_BLOCK = Event.create();
    public static final Event<AttemptBlockPlacement> ATTEMPT_BLOCK_PLACEMENT = Event.withResult();
    public static final Event<BlockPlaced> BLOCK_PLACED = Event.create();
    public static final Event<AttemptBlockRemoval> ATTEMPT_BLOCK_REMOVAL = Event.withResult();
    public static final Event<BlockRemoved> BLOCK_REMOVED = Event.create();

    @FunctionalInterface
    public interface SetBlock {
        void onSetBlock(World world, BlockPos pos, Block block);
    }

    @FunctionalInterface
    public interface AttemptBlockPlacement {
        EventResult onAttemptBlockPlacement(Player player, Block placed, BlockPos pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface BlockPlaced {
        void onBlockPlaced(Player player, Block placed, BlockPos pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface AttemptBlockRemoval {
        EventResult onAttemptBlockRemoval(ServerPlayer player, Block removed, BlockPos pos, ItemStack stack);
    }

    @FunctionalInterface
    public interface BlockRemoved {
        void onBlockRemoved(ServerPlayer player, Block removed, BlockPos pos, ItemStack stack);
    }
}
