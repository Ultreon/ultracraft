package com.ultreon.craft.item;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.NotNull;

public class BlockItem extends Item {
    private final Block block;

    public BlockItem(Block block) {
        super(null);
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }

    @Override
    public void use(UseItemContext useItemContext) {
        super.use(useItemContext);

        World world = useItemContext.world();
        Vec3i next = useItemContext.result().getNext();
        if (!world.intersectEntities(this.block.getBoundingBox(next))) {
            world.set(next, this.block);
        }
    }

    @Override
    public String getTranslation() {
        return this.block.getTranslation();
    }

    @NotNull
    @Override
    public String getTranslationId() {
        return this.block.getTranslationId();
    }
}
