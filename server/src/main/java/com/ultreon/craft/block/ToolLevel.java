package com.ultreon.craft.block;

import com.ultreon.craft.ToolLevels;
import com.ultreon.craft.tags.NamedTag;

public class ToolLevel {
    public static final ToolLevel WOOD = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_WOODEN_TOOL));
    public static final ToolLevel STONE = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_STONE_TOOL));
    public static final ToolLevel COPPER = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_COPPER_TOOL));
    public static final ToolLevel IRON = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_IRON_TOOL));
    public static final ToolLevel GOLD = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_GOLDEN_TOOL));
    public static final ToolLevel DIAMOND = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_DIAMOND_TOOL));
    public static final ToolLevel PLATINUM = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_PLATINUM_TOOL));
    public static final ToolLevel COBALT = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_COBALT_TOOL));
    public static final ToolLevel TITANIUM = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_TITANIUM_TOOL));
    public static final ToolLevel CHUNK_STEEL = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_CHUNK_STEEL_TOOL));
    public static final ToolLevel ULTRINIUM = ToolLevels.register(new ToolLevel(BlockTags.REQUIRES_ULTRINIUM_TOOL));

    private final NamedTag<Block> tag;

    private ToolLevel(NamedTag<Block> tag) {
        this.tag = tag;
    }

    public NamedTag<Block> tag() {
        return tag;
    }

    @Override
    public String toString() {
        return "ToolRequirement[" +
                "tag=" + tag + ']';
    }
}
