package com.ultreon.craft.world.gen.feature;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.gen.WorldAccess;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchFeature extends WorldGenFeature {
    private final NoiseConfig settingsBase;
    private final Block patchBlock;
    private final float threshold;
    @LazyInit
    @Nullable
    private NoiseInstance baseNoise;
    private final int depth;

    /**
     * Creates a new patch feature with the given settings
     *
     * @param settingsBase the noise config to use
     * @param patchBlock   the block to use for the patch
     * @param threshold    the threshold to use for the patch
     * @deprecated Use {@link #PatchFeature(NoiseConfig, Block, float, int)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public PatchFeature(NoiseConfig settingsBase, Block patchBlock, float threshold) {
        this(settingsBase, patchBlock, threshold, 4);
    }

    /**
     * Creates a new patch feature with the given settings
     *
     * @param settingsBase the noise config to use
     * @param patchBlock   the block to use for the patch
     * @param threshold    the threshold to use for the patch
     * @param depth        the depth for the patch generation.
     */
    public PatchFeature(NoiseConfig settingsBase, Block patchBlock, float threshold, int depth) {
        this.settingsBase = settingsBase;
        this.patchBlock = patchBlock;
        this.threshold = threshold;
        this.depth = depth;
    }

    @Override
    public void create(@NotNull ServerWorld world) {
        super.create(world);

        this.baseNoise = this.settingsBase.create(world.getSeed());
    }

    @Override
    public boolean handle(@NotNull WorldAccess world, int x, int y, int z, int height) {
        if (y != height) return false;
        if (this.baseNoise == null) return false;

        for (int yOffset = y; yOffset > y - this.depth; yOffset--) {
            float value = (float) this.baseNoise.eval(x, yOffset, z);
            if (value < this.threshold && world.set(x, yOffset, z, this.patchBlock)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void dispose() {
        if (this.baseNoise != null) this.baseNoise.dispose();
    }
}
