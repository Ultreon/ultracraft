package com.ultreon.craft.client.network;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ChunkSection;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class PartialChunkData {
    private final ChunkPos pos;
    private final ChunkSection[] sections = new ChunkSection[CHUNK_HEIGHT / CHUNK_SIZE];
    private PaletteStorage<Biome> biomes;

    public PartialChunkData(ChunkPos pos) {
        this.pos = pos;
    }

    public void setBiomes(PaletteStorage<Biome> biomes) {
        this.biomes = biomes;
    }

    public synchronized void setSection(int y, long[] data, Block[] palette) {
        ChunkSection section = this.sections[y];
        if (section != null) {
            section.dispose();
        }
        this.sections[y] = new ChunkSection(data, palette);
    }

    public boolean isBuilt() {
        for (ChunkSection section : this.sections)
            if (section == null) return false;

        return this.biomes != null;
    }

    public ClientChunk build(ClientWorld world) {
        ClientChunk chunk = new ClientChunk(world, pos, this.sections, this.biomes);
        this.sections[0] = null;
        this.biomes = null;
        return chunk;
    }
}
