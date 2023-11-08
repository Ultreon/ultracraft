package com.ultreon.craft.item;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.BlockPos;
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
    public void use(UseItemContext useItemContext) {
        super.use(useItemContext);

        World world = useItemContext.world();
        ItemStack stack = useItemContext.stack();
        Vec3i next = useItemContext.result().getNext();
        if (useItemContext.world().isServerSide() && !world.intersectEntities(this.getBlock().getBoundingBox(next))) {
            world.set(new BlockPos(next), this.getBlock());
            stack.shrink(1);
        }
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
}