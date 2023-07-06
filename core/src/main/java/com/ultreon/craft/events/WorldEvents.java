package com.ultreon.craft.events;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.libs.events.v1.Event;

import java.util.List;

public class WorldEvents {
    public static final Event<PreTick> PRE_TICK = Event.create();
    public static final Event<PostTick> POST_TICK = Event.create();
    public static final Event<ChunkGenerated> CHUNK_GENERATED = Event.create();
    public static final Event<ChunkLoaded> CHUNK_LOADED = Event.create();
    public static final Event<ChunkUnloaded> CHUNK_UNLOADED = Event.create();
    public static final Event<CreateBiome> CREATE_BIOME = Event.create();

    @FunctionalInterface
    public interface PreTick {
        void onPreTick(World world);
    }

    @FunctionalInterface
    public interface PostTick {
        void onPostTick(World world);
    }

    @FunctionalInterface
    public interface ChunkGenerated {
        void onChunkGenerated(World world, ChunkPos pos, Chunk chunk);
    }

    @FunctionalInterface
    public interface ChunkLoaded {
        void onChunkLoaded(World world, ChunkPos pos, Chunk chunk);
    }

    @FunctionalInterface
    public interface ChunkUnloaded {
        void onChunkUnloaded(World world, ChunkPos pos, Chunk chunk);
    }

    @FunctionalInterface
    public interface CreateBiome {
        void onCreateBiome(World world, NoiseInstance noiseInstance, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers);
    }
}
