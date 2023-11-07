package com.ultreon.craft.world.gen.layer;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import org.jetbrains.annotations.Nullable;

public class StonePatchTerrainLayer extends TerrainLayer {
    private final NoiseSettings settingsX;
    private final NoiseSettings settingsY;
    private final NoiseSettings settingsBase;
    public float stoneThreshold = 0f;
    @LazyInit
    @Nullable
    private NoiseInstance baseNoise;

    @LazyInit
    @Nullable
    public DomainWarping domainWarping = null;

    public StonePatchTerrainLayer(NoiseSettings settingsX, NoiseSettings settingsY, NoiseSettings settingsBase) {
        this.settingsX = settingsX;
        this.settingsY = settingsY;
        this.settingsBase = settingsBase;
    }

    @Override
    public void create(ServerWorld world) {
        super.create(world);

        this.domainWarping = new DomainWarping(world.getSeed(), this.settingsX, this.settingsY);
        this.baseNoise = this.settingsBase.create(world.getSeed());
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (this.domainWarping == null) return false;
        if (this.baseNoise == null) return false;

        float value = this.domainWarping.generateDomainNoise(x, z, this.baseNoise);
        if (value > this.stoneThreshold) {
            chunk.set(x, y, z, Blocks.STONE);
            return true;
        }

        return false;
    }

    @Override
    public void dispose() {
        if (this.domainWarping != null) this.domainWarping.dispose();
        if (this.baseNoise != null) this.baseNoise.dispose();
    }
}
