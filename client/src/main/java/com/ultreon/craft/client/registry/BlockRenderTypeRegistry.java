package com.ultreon.craft.client.registry;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.render.RenderType;

import java.util.HashMap;
import java.util.Map;

public class BlockRenderTypeRegistry {
    private static final Map<Block, RenderType> registry = new HashMap<>();

    public static void register(Block block, RenderType model) {
        BlockRenderTypeRegistry.registry.put(block, model);
    }

    public static RenderType get(Block block) {
        return BlockRenderTypeRegistry.registry.getOrDefault(block, RenderType.DEFAULT);
    }
}
