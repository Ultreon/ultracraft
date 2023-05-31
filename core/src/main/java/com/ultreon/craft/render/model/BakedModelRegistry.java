package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;

public record BakedModelRegistry(TextureAtlas atlas, ImmutableMap<Block, BakedCubeModel> bakedModels) {
}
