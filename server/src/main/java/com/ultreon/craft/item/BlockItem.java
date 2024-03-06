package com.ultreon.craft.item;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.events.BlockEvents;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.UseResult;
import com.ultreon.craft.world.ServerWorld;
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

        World world = useItemContext.world();
        ItemStack stack = useItemContext.stack();
        Vec3i pos = useItemContext.result().getPos();
        Vec3i next = useItemContext.result().getNext();
        BlockPos blockPos = new BlockPos(next);
        BlockEvents.ATTEMPT_BLOCK_PLACEMENT.factory().onAttemptBlockPlacement(useItemContext.player(), this.block.get(), blockPos, useItemContext.stack());
        if (world.get(pos.x, pos.y, pos.z).isReplaceable()) {
            if (!world.intersectEntities(this.getBlock().getBoundingBox(pos))) {
                world.set(new BlockPos(pos), this.getBlock().createMeta());
                stack.shrink(1);
                return UseResult.ALLOW;
            }
            if (world instanceof ServerWorld serverWorld) serverWorld.update(pos);
            return UseResult.DENY;
        }

        if (!world.intersectEntities(this.getBlock().getBoundingBox(next))) {
            world.set(blockPos, this.getBlock().createMeta());
            stack.shrink(1);

            BlockEvents.BLOCK_PLACED.factory().onBlockPlaced(useItemContext.player(), this.block.get(), blockPos, useItemContext.stack());

            return UseResult.ALLOW;
        }
        if (world instanceof ServerWorld serverWorld) serverWorld.update(next);
        return UseResult.DENY;
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
