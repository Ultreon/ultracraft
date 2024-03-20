package com.ultreon.craft.item;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.BlockEvents;
import com.ultreon.craft.events.api.EventResult;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.CubicDirection;
import com.ultreon.craft.world.UseResult;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BlockItem extends Item {
    private final @NotNull Supplier<Block> block;

    public BlockItem(Properties properties, @NotNull Supplier<Block> block) {
        super(properties);
        Preconditions.checkNotNull(block, "block");
        this.block = Suppliers.memoize(block::get);
    }

    public Block getBlock() {
        return this.block.get();
    }

    @Override
    public UseResult use(UseItemContext useItemContext) {
        super.use(useItemContext);

        var world = useItemContext.world();
        var stack = useItemContext.stack();
        var pos = useItemContext.result().getPos();
        var next = useItemContext.result().getNext();
        var direction = useItemContext.result().direction;
        var player = useItemContext.player();

        BlockPos blockPos = new BlockPos(next);
        EventResult eventResult = BlockEvents.ATTEMPT_BLOCK_PLACEMENT.factory()
                .onAttemptBlockPlacement(player, this.block.get(), blockPos, stack);

        if (eventResult.isCanceled()) return UseResult.DENY;

        if (!block.get().canBePlacedAt(world, blockPos, player, stack, direction))
            return UseResult.DENY;

        BlockMetadata oldBlock = world.get(pos.x, pos.y, pos.z);
        return oldBlock.isReplaceable() && oldBlock.canBeReplacedBy(useItemContext)
                ? replaceBlock(world, pos, player, stack, direction)
                : placeBlock(world, next, blockPos, player, stack, direction);

    }

    @NotNull
    private UseResult placeBlock(World world, Vec3i next, BlockPos blockPos, Player player, ItemStack stack, CubicDirection direction) {
        if (world.intersectEntities(this.getBlock().getBoundingBox(next)))
            return UseResult.DENY;

        if (world.isClientSide()) {
            var state = this.getBlock().onPlacedBy(world, blockPos, createBlockMeta(), player, stack, direction);
            world.set(blockPos, state);
        }

        BlockEvents.BLOCK_PLACED.factory().onBlockPlaced(player, this.block.get(), blockPos, stack);

        stack.shrink(1);
        return UseResult.ALLOW;
    }

    @NotNull
    private UseResult replaceBlock(World world, Vec3i vec, Player player, ItemStack stack, CubicDirection direction) {
        if (world.intersectEntities(this.getBlock().getBoundingBox(vec)))
            return UseResult.DENY;

        if (world.isClientSide()) {
            BlockPos blockPos = new BlockPos(vec);
            var state = this.getBlock().onPlacedBy(world, blockPos, createBlockMeta(), player, stack, direction);
            world.set(blockPos, state);
        }

        stack.shrink(1);
        return UseResult.ALLOW;
    }

    @Override
    public TextObject getTranslation() {
        return this.block.get().getTranslation();
    }

    @NotNull
    @Override
    public String getTranslationId() {
        return this.block.get().getTranslationId();
    }

    public BlockMetadata createBlockMeta() {
        return this.block.get().createMeta();
    }
}
