package com.ultreon.craft.world.gen.feature;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FiolageFeature extends WorldGenFeature {
    private final NoiseConfig settingsBase;
    private final Block fiolageBlock;
    private final float threshold;
    @LazyInit
    @Nullable
    private NoiseInstance baseNoise;

    /**
     * Creates a new fiolage feature with the given settings
     *
     * @param fiolageBlock the block to use for the fiolage
     * @param threshold    the threshold to use for the fiolage
     */
    public FiolageFeature(Block fiolageBlock, float threshold) {
        this.settingsBase = NoiseConfigs.FIOLAGE;
        this.fiolageBlock = fiolageBlock;
        this.threshold = threshold;
    }

    @Override
    public void create(@NotNull ServerWorld world) {
        super.create(world);

        this.baseNoise = this.settingsBase.create(world.getSeed());
    }

    @Override
    public boolean handle(@NotNull World world, @NotNull Chunk chunk, int x, int z, int height) {
        if (this.baseNoise == null) return false;

        float value = (float) this.baseNoise.eval(chunk.getOffset().x + x, 69, chunk.getOffset().z + z);
        return value < this.threshold && chunk.set(x, height + 1, z, this.fiolageBlock);
    }

    @Override
    public void dispose() {
        if (this.baseNoise != null) this.baseNoise.dispose();
    }
}
