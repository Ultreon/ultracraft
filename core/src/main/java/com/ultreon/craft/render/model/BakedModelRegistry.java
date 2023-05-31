package com.ultreon.craft.render.model;

import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.render.texture.atlas.TextureAtlas;

public record BakedModelRegistry(TextureAtlas atlas, ImmutableMap<Block, BakedCubeModel> bakedModels) {
}
