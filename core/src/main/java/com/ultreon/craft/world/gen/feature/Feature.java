package com.ultreon.craft.world.gen.feature;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.GenerationStage;

import java.util.Random;

public abstract class Feature {
    private GenerationStage stage;

    protected Feature(GenerationStage stage) {
        this.stage = stage;
    }

    public abstract void generate(World world, Chunk chunk, int x, int y, int z, Random random);
    public abstract boolean canGenerate(World world, Chunk chunk, int x, int y, int z, Random random);

    public <E> GenerationStage stage() {
        return this.stage;
    }
}
