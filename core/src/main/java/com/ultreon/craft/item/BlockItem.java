package com.ultreon.craft.item;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockItem extends Item {
    private final Block block;

    public BlockItem(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }

    @Override
    public void use(UseItemContext useItemContext) {
        super.use(useItemContext);

        World world = useItemContext.world();
        GridPoint3 next = useItemContext.result().getNext();
        if (!world.intersectEntities(block.getBoundingBox(next))) {
            world.set(next, this.block);
        }
    }

    @Override
    public String getTranslation() {
        return block.getTranslation();
    }

    @NotNull
    @Override
    public String getTranslationId() {
        return block.getTranslationId();
    }
}
