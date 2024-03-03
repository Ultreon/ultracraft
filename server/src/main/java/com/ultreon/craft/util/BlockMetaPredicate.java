package com.ultreon.craft.util;

import com.ultreon.craft.block.state.BlockMetadata;

import java.util.function.Predicate;

public class BlockMetaPredicate implements Predicate<BlockMetadata> {
    public static final BlockMetaPredicate TRANSPARENT = new BlockMetaPredicate(BlockMetadata::isTransparent);
    public static final BlockMetaPredicate FLUID = new BlockMetaPredicate(BlockMetadata::isFluid);
    public static final BlockMetaPredicate SOLID = new BlockMetaPredicate(BlockMetadata::hasCollider);
    public static final BlockMetaPredicate NON_FLUID = new BlockMetaPredicate(block -> !block.isFluid());
    public static final BlockMetaPredicate REPLACEABLE = new BlockMetaPredicate(BlockMetadata::isReplaceable);
    public static final BlockMetaPredicate BREAK_INSTANTLY = new BlockMetaPredicate(block -> block.getHardness() == 0);
    public static final BlockMetaPredicate EVERYTHING = new BlockMetaPredicate(block -> true);

    private final Predicate<BlockMetadata> predicate;

    BlockMetaPredicate(Predicate<BlockMetadata> predicate) {

        this.predicate = predicate;
    }

    @Override
    public boolean test(BlockMetadata block) {
        return this.predicate.test(block);
    }
}
