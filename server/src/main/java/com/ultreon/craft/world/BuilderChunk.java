package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.collection.FlatStorage;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.List;

public final class BuilderChunk extends Chunk {
    private final ServerWorld world;
    private final Thread thread;
    private final FlatStorage<BiomeGenerator> biomeData = new FlatStorage<>(256);
    private List<Vec3i> biomeCenters;

    public BuilderChunk(ServerWorld world, Thread thread, int size, int height, ChunkPos pos) {
        super(world, size, height, pos);
        this.world = world;
        this.thread = thread;
    }

    @Override
    public Block getFast(int x, int y, int z) {
        if (!this.isOnBuilderThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.getFast(x, y, z);
    }

    @Override
    public boolean setFast(int x, int y, int z, Block block) {
        if (!this.isOnBuilderThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.setFast(x, y, z, block);
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isOnBuilderThread() {
        return this.thread.getId() == Thread.currentThread().getId();
    }

    public ServerChunk build() {
        ServerChunk builtChunk = new ServerChunk(this.world, this.size, this.height, World.toLocalChunkPos(this.getPos()), this.storage);
        this.biomeData.map(BiomeGenerator::getBiome, Biome.class);
        return builtChunk;
    }

    public void setBiomeGenerator(int x, int z, BiomeGenerator generator) {
        int index = this.toFlatIndex(x, z);
        this.biomeData.set(index, generator);
    }

    public BiomeGenerator getBiomeGenerator(int x, int z) {
        int index = this.toFlatIndex(x, z);
        return this.biomeData.get(index);
    }

    public void setBiomeCenters(List<Vec3i> biomeCenters) {
        this.biomeCenters = biomeCenters;
    }

    public List<Vec3i> getBiomeCenters() {
        return this.biomeCenters;
    }
}
