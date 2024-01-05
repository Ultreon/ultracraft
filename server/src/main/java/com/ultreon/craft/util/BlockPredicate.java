package com.ultreon.craft.util;

import com.ultreon.craft.block.Block;

import java.util.function.Predicate;

public class BlockPredicate implements Predicate<Block> {
    public static final BlockPredicate TRANSPARENT = new BlockPredicate(Block::isTransparent);
    public static final BlockPredicate FLUID = new BlockPredicate(Block::isFluid);
    public static final BlockPredicate SOLID = new BlockPredicate(Block::hasCollider);
    public static final BlockPredicate NON_FLUID = new BlockPredicate(block -> !block.isFluid());
    public static final BlockPredicate REPLACEABLE = new BlockPredicate(block -> block.isReplaceable());
    public static final BlockPredicate BREAK_INSTANTLY = new BlockPredicate(block -> block.getHardness() == 0);
    public static final BlockPredicate EVERYTHING = new BlockPredicate(block -> true);

    private final Predicate<Block> predicate;

    BlockPredicate(Predicate<Block> predicate) {

        this.predicate = predicate;
    }

    @Override
    public boolean test(Block block) {
        return this.predicate.test(block);
    }
}
