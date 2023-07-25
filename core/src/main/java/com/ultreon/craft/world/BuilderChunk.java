package com.ultreon.craft.world;

public class BuilderChunk extends Chunk {
    public BuilderChunk(World world, int size, int height, ChunkPos pos) {
        super(world, size, height, pos);
    }

    BuilderChunk(World world, int size, int height, ChunkPos pos, Heightmap heightmap) {
        super(world, size, height, pos, heightmap);
    }

    public CompletedChunk build() {
        return new CompletedChunk(this.world, this.size, this.height, this.pos, this.heightmap);
    }
}
