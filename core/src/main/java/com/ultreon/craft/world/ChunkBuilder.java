package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.Mesh;

public class ChunkBuilder {
    private final Chunk chunk;

    public ChunkBuilder(World world, ChunkPos pos) {
        this.chunk = new Chunk(world, World.CHUNK_SIZE, World.CHUNK_HEIGHT, pos);
    }

    public ChunkBuilder mesh(Mesh mesh) {
        this.chunk.mesh = mesh;
        return this;
    }
}
