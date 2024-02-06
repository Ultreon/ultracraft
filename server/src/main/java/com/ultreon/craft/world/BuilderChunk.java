package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.collection.FlatStorage;
import com.ultreon.craft.collection.Storage;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.List;

public final class BuilderChunk extends Chunk {
    private final ServerWorld world;
    private final Thread thread;
    private final FlatStorage<BiomeGenerator> biomeData = new FlatStorage<>(256);
    private List<Vec3i> biomeCenters;
    private final ServerWorld.Region region;

    public BuilderChunk(ServerWorld world, Thread thread, ChunkPos pos, ServerWorld.Region region) {
        super(world, pos);
        this.world = world;
        this.thread = thread;
        this.region = region;
    }

    @Override
    public Block getFast(int x, int y, int z) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.getFast(x, y, z);
    }

    @Override
    public void set(Vec3i pos, Block block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(pos.x, pos.y, pos.z)) {
            this.world.recordOutOfBounds(this.offset.x + pos.x, this.offset.y + pos.y, this.offset.z + pos.z, block);
            return;
        }
        super.set(pos, block);
    }

    @Override
    public boolean set(int x, int y, int z, Block block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(x, y, z)) {
            this.world.recordOutOfBounds(this.offset.x + x, this.offset.y + y, this.offset.z + z, block);
            return false;
        }
        return super.set(x, y, z, block);
    }

    @Override
    public void setFast(Vec3i pos, Block block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(pos.x, pos.y, pos.z)) {
            this.world.recordOutOfBounds(this.offset.x + pos.x, this.offset.y + pos.y, this.offset.z + pos.z, block);
            return;
        }
        super.setFast(pos, block);
    }

    @Override
    public boolean setFast(int x, int y, int z, Block block) {
        if (this.isOnInvalidThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        if (this.isOutOfBounds(x, y, z)) {
            this.world.recordOutOfBounds(this.offset.x + x, this.offset.y + y, this.offset.z + z, block);
            return false;
        }
        return super.setFast(x, y, z, block);
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isOnInvalidThread() {
        return this.thread.getId() != Thread.currentThread().getId();
    }

    public boolean isOnBuilderThread() {
        return this.thread.getId() == Thread.currentThread().getId();
    }

    public ServerChunk build() {
        Storage<Biome> map = this.biomeData.map(BiomeGenerator::getBiome, Biome.class);
        return new ServerChunk(this.world, World.toLocalChunkPos(this.getPos()), this.storage, map, region);
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

    public LightMap getLightMap() {
        return this.lightMap;
    }
}
