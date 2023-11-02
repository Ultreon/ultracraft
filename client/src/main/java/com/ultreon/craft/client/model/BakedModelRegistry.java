package com.ultreon.craft.client.model;

import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.atlas.TextureAtlas;

import java.util.Objects;

public final class BakedModelRegistry {
    private final TextureAtlas atlas;
    private final ImmutableMap<Block, BakedCubeModel> bakedModels;

    public BakedModelRegistry(TextureAtlas atlas, ImmutableMap<Block, BakedCubeModel> bakedModels) {
        this.atlas = atlas;
        this.bakedModels = bakedModels;
    }

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public ImmutableMap<Block, BakedCubeModel> bakedModels() {
        return this.bakedModels;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BakedModelRegistry that = (BakedModelRegistry) obj;
        return Objects.equals(this.atlas, that.atlas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.atlas);
    }

    @Override
    public String toString() {
        return "BakedModelRegistry[" +
                "atlas=" + this.atlas + ']';
    }
}