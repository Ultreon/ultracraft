package com.ultreon.craft.events;

import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.events.v1.Event;

import java.util.List;

public class WorldEvents {
    public static final Event<PreTick> PRE_TICK = Event.create();
    public static final Event<PostTick> POST_TICK = Event.create();
    public static final Event<ChunkGenerated> CHUNK_GENERATED = Event.create();
    public static final Event<ChunkLoaded> CHUNK_LOADED = Event.create();
    public static final Event<ChunkUnloaded> CHUNK_UNLOADED = Event.create();
    public static final Event<CreateBiome> CREATE_BIOME = Event.create();
    public static final Event<SaveWorld> SAVE_WORLD = Event.create();
    public static final Event<LoadWorld> LOAD_WORLD = Event.create();
    public static final Event<SaveRegion> SAVE_REGION = Event.create();
    public static final Event<LoadRegion> LOAD_REGION = Event.create();
    public static final Event<SaveChunk> SAVE_CHUNK = Event.create();
    public static final Event<LoadChunk> LOAD_CHUNK = Event.create();

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

    @FunctionalInterface
    public interface SaveWorld {
        void onSaveWorld(World world, SavedWorld save);
    }

    @FunctionalInterface
    public interface LoadWorld {
        void onLoadWorld(World world, SavedWorld save);
    }

    @FunctionalInterface
    public interface SaveRegion {
        void onSaveRegion(World world, WorldRegion region);
    }

    @FunctionalInterface
    public interface LoadRegion {
        void onLoadRegion(World world, WorldRegion region);
    }

    @FunctionalInterface
    public interface SaveChunk {
        void onSaveChunk(Chunk region, MapType extraData);
    }

    @FunctionalInterface
    public interface LoadChunk {
        void onLoadChunk(Chunk region, MapType extraData);
    }
}
