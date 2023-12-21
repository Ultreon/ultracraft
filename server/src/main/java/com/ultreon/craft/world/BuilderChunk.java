package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.List;

import static com.ultreon.craft.world.World.*;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public final class BuilderChunk extends Chunk {
    private final ServerWorld world;
    private final Thread thread;
    private final PaletteStorage<BiomeGenerator> generators = new PaletteStorage<>(256, BiomeGenerator.EMPTY);
    private List<Vec3i> biomeCenters;

    public BuilderChunk(ServerWorld world, Thread thread, ChunkPos pos) {
        super(world, pos);

        this.world = world;
        this.thread = thread;
    }

    @Override
    public Block getFast(int x, int y, int z) {
        if (!this.isOnBuilderThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.getFast(localizePos(x), localizePos(y), localizePos(z));
    }

    @Override
    public boolean setFast(int x, int y, int z, Block block) {
        if (!this.isOnBuilderThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.setFast(localizePos(x), localizePos(y), localizePos(z), block);
    }

    @Override
    public Block get(int x, int y, int z) {
        return this.getFast(x, y, z);
    }

    @Override
    public boolean set(int x, int y, int z, Block block) {
        return this.setFast(x, y, z, block);
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isOnBuilderThread() {
        return this.thread.getId() == Thread.currentThread().getId();
    }

    public ServerChunk build() {
        PaletteStorage<Biome> biomes = this.generators.map(BiomeGenerator::getBiome);
        return new ServerChunk(this.world, toLocalChunkPos(this.getPos()), biomes);
    }

    public void setBiomeGenerator(int x, int z, BiomeGenerator generator) {
        int index = this.toFlatIndex(x, z);
        this.generators.set(index, generator);
    }

    public BiomeGenerator getBiomeGenerator(int x, int z) {
        int index = this.toFlatIndex(x, z);
        return this.generators.get(index);
    }

    public void setBiomeCenters(List<Vec3i> biomeCenters) {
        this.biomeCenters = biomeCenters;
    }

    public List<Vec3i> getBiomeCenters() {
        return this.biomeCenters;
    }
}
