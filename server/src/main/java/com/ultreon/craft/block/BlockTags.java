package com.ultreon.craft.block;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.tags.NamedTag;
import com.ultreon.craft.util.Identifier;

public class BlockTags {
    public static final NamedTag<Block> REQUIRES_WOODEN_TOOL = create("block/requires_wooden_tool");
    public static final NamedTag<Block> REQUIRES_STONE_TOOL = create("block/requires_stone_tool");
    public static final NamedTag<Block> REQUIRES_COPPER_TOOL = create("block/requires_copper_tool");
    public static final NamedTag<Block> REQUIRES_GOLDEN_TOOL = create("block/requires_golden_tool");
    public static final NamedTag<Block> REQUIRES_IRON_TOOL = create("block/requires_iron_tool");
    public static final NamedTag<Block> REQUIRES_DIAMOND_TOOL = create("block/requires_diamond_tool");
    public static final NamedTag<Block> REQUIRES_PLATINUM_TOOL = create("block/requires_platinum_tool");
    public static final NamedTag<Block> REQUIRES_COBALT_TOOL = create("block/requires_cobalt_tool");
    public static final NamedTag<Block> REQUIRES_TITANIUM_TOOL = create("block/requires_titanium_tool");
    public static final NamedTag<Block> REQUIRES_CHUNK_STEEL_TOOL = create("block/requires_chunk_steel_tool");
    public static final NamedTag<Block> REQUIRES_ULTRINIUM_TOOL = create("block/requires_ultrinium_tool");

    private static NamedTag<Block> create(String name) {
        return Registries.BLOCK.createTag(new Identifier(name));
    }
}
