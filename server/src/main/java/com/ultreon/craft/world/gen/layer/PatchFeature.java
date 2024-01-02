package com.ultreon.craft.world.gen.layer;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.Nullable;

public class PatchFeature extends WorldGenFeature {
    private final NoiseConfig settingsBase;
    private final Block patchBlock;
    public final float stoneThreshold;
    @LazyInit
    @Nullable
    private NoiseInstance baseNoise;

    public PatchFeature(NoiseConfig settingsBase, Block patchBlock, float stoneThreshold) {
        this.settingsBase = settingsBase;
        this.patchBlock = patchBlock;
        this.stoneThreshold = stoneThreshold;
    }

    @Override
    public void create(ServerWorld world) {
        super.create(world);

        this.baseNoise = this.settingsBase.create(world.getSeed());
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int z, int height) {
        if (this.baseNoise == null) return false;

        float value = (float) this.baseNoise.eval(chunk.getOffset().x + x, chunk.getOffset().z + z);
        if (value < this.stoneThreshold) {
            chunk.set(x, height, z, this.patchBlock);
            return true;
        }

        return false;
    }

    @Override
    public void dispose() {
        if (this.baseNoise != null) this.baseNoise.dispose();
    }
}
