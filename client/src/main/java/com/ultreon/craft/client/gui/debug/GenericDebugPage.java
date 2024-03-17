package com.ultreon.craft.client.gui.debug;

import com.badlogic.gdx.graphics.Mesh;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.util.RenderableArray;
import com.ultreon.craft.client.world.ChunkMesh;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

public class GenericDebugPage implements DebugPage {
    @Override
    public void render(DebugRenderContext context) {
        var client = context.client();
        var world = client.world;
        var worldRenderer = client.worldRenderer;
        if (worldRenderer != null && worldRenderer.isDisposed()) worldRenderer = null;
        if (world != null && world.isDisposed()) world = null;

        context.left("GDX Status")
                .left("Mesh Status", Mesh.getManagedStatus())
                .left();

        // World
        @Nullable IntegratedServer integratedServer = client.integratedServer;
        if (integratedServer != null) {
            context.left("Integrated Server")
                    .left("Server TPS", integratedServer.getCurrentTps())
                    .left("Packets", "rx = " + Connection.getPacketsReceived() + ", tx = " + Connection.getPacketsSent())
                    .left();
        } else {
            context.left("Server Connection")
                    .left("Server TPS", "N/A (Coming Soon!)")
                    .left("Packets", "rx = " + Connection.getPacketsReceived() + ", tx = " + Connection.getPacketsSent())
                    .left();
        }

        context.left("Meshes")
                .left("Meshes Disposed", ChunkMesh.getMeshesDisposed())
                .left("Vertex Count", WorldRenderer.getVertexCount())
                .left();

        context.left("Renderables")
                .left("Global Size", RenderableArray.getGlobalSize())
                .left("Obtained Renderables", ValueTracker.getObtainedRenderables())
                .left("Obtain Requests", ValueTracker.getObtainRequests())
                .left("Free Requests", ValueTracker.getFreeRequests())
                .left("Flush Requests", ValueTracker.getFlushRequests())
                .left();

        if (world != null) {
            // Player
            Player player = client.player;
            if (player != null) {
                context.left("Player");
                BlockPos blockPosition = player.getBlockPos();
                Vec3i sectionPos = context.block2sectionPos(blockPosition);
                ChunkPos chunkPos = player.getChunkPos();
                ClientChunk chunk = world.getChunk(chunkPos);
                BlockPos localBlockPos = World.toLocalBlockPos(blockPosition);

                context.left("XYZ", player.getPosition())
                        .left("Block XYZ", blockPosition)
                        .left("Chunk XYZ", sectionPos)
                        .left("Biome", Registries.BIOME.getId(world.getBiome(blockPosition)));
                if (chunk != null) {
                    int sunlight = chunk.getSunlight(localBlockPos.vec());
                    int blockLight = chunk.getBlockLight(localBlockPos.vec());

                    context.left("Chunk Offset", chunk.renderOffset)
                            .left("Sunlight", sunlight)
                            .left("Block Light", blockLight);
                }
                context.left("Chunk Shown", world.getChunk(chunkPos) != null);
                HitResult hitResult = client.hitResult;
                if (hitResult != null)
                    context.left("Break Progress", world.getBreakProgress(new BlockPos(hitResult.getPos())));
                context.left();
            }

            context.left("World");
            if (worldRenderer != null) {
                context.left("Visible Chunks", worldRenderer.getVisibleChunks() + "/" + worldRenderer.getLoadedChunks());
            }

            context.left("Chunk Mesh Disposes", WorldRenderer.getChunkMeshFrees());
            if (client.isSinglePlayer()) {
                context.left("Chunk Loads", ValueTracker.getChunkLoads())
                        .left("Chunk Unloads", ServerWorld.getChunkUnloads())
                        .left("Chunk Saves", ServerWorld.getChunkSaves());
            }

            context.left("Pool Free", WorldRenderer.getPoolFree())
                    .left("Pool Max", WorldRenderer.getPoolMax())
                    .left("Pool Peak", WorldRenderer.getPoolPeak())
                    .left();
        }

        HitResult cursor = client.cursor;
        if (cursor.isCollide()) {
            BlockMetadata block = cursor.blockMeta;
            if (block != null && !block.isAir()) {
                context.right("Block", block);
            }
        }
    }
}
