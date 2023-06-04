package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;

public class BuilderChunk extends RawChunk {
    public BuilderChunk(World world, int size, int height, ChunkPos pos) {
        super(world, size, height, pos);
    }

    BuilderChunk(World world, int size, int height, ChunkPos pos, Block[] blocks, Heightmap heightmap) {
        super(world, size, height, pos, blocks, heightmap);
    }

    public Chunk build() {
        Chunk chunk = new Chunk(this.world, this.size, this.height, this.pos, this.blocks, this.heightmap);
        this.blocks = null;
        return chunk;
    }
}
