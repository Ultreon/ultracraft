package com.ultreon.craft.client.gui;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.GamePlatform;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.util.HitResult;
import com.badlogic.gdx.graphics.Mesh;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.client.world.ChunkMesh;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

public class DebugGui {
    private static final int OFFSET = 20;
    private final UltracraftClient client;
    private int y;

    public DebugGui(UltracraftClient client) {
        this.client = client;
    }

    public void render(Renderer renderer) {
        this.y = DebugGui.OFFSET;

        ClientWorld world = this.client.world;
        @Nullable WorldRenderer worldRenderer = this.client.worldRenderer;
        if (worldRenderer != null && worldRenderer.isDisposed()) worldRenderer = null;
        if (world != null && world.isDisposed()) world = null;

        this.drawLine(renderer, Mesh.getManagedStatus());

        // World
        if (this.client.integratedServer != null) {
            this.drawLine(renderer, "server", this.client.integratedServer);
            this.drawLine(renderer, "server tps", this.client.integratedServer.getCurrentTps());
            this.drawLine(renderer, "server tps", this.client.getCurrentTps());
            this.drawLine(renderer, "packets", "recv " + Connection.getPacketsReceived() + " | sent " + Connection.getPacketsSent());
            this.drawLine(renderer, "server xyz", this.client.integratedServer.getPlayers().stream().findFirst().map(Entity::getPosition).orElse(null));
        }
        if (world != null) {
            this.drawLine(renderer, "fps", Gdx.graphics.getFramesPerSecond());
            // Player
            Player player = this.client.player;
            if (player != null) {
                BlockPos blockPosition = player.blockPosition();
                Vec3i sectionPos = this.block2sectionPos(blockPosition);
                ChunkPos chunkPos = player.getChunkPos();
                ClientChunk chunk = world.getChunk(chunkPos);
                this.drawLine(renderer, "xyz", player.getPosition());
                this.drawLine(renderer, "block xyz", blockPosition);
                this.drawLine(renderer, "chunk xyz", sectionPos);
                this.drawLine(renderer, "chunk xz", chunkPos);
                if (chunk != null) {
                    this.drawLine(renderer, "chunk render offset", chunk.renderOffset);
                }
                this.drawLine(renderer, "chunk shown", world.getChunk(chunkPos) != null);
                HitResult hitResult = this.client.hitResult;
                if (hitResult != null) this.drawLine(renderer, "break progress", world.getBreakProgress(new BlockPos(hitResult.getPos())));
            }

            this.drawLine(renderer, "meshes disposed", ChunkMesh.getMeshesDisposed());
//            this.drawLine(renderer, "meshes built", ChunkMeshBuilder.getMeshesBuilt());
            this.drawLine(renderer, "vertex count", WorldRenderer.getVertexCount());

            if (worldRenderer != null) {
                this.drawLine(renderer, "visible chunks", worldRenderer.getVisibleChunks() + "/" + worldRenderer.getLoadedChunks());
            }

            this.drawLine(renderer, "chunk mesh disposes", WorldRenderer.getChunkMeshFrees());
            if (this.client.isSinglePlayer()) {
                this.drawLine(renderer, "chunk loads", ServerWorld.getChunkLoads());
                this.drawLine(renderer, "chunk unloads", ServerWorld.getChunkUnloads());
                this.drawLine(renderer, "chunk saves", ServerWorld.getChunkSaves());
            }
            this.drawLine(renderer, "pool free", WorldRenderer.getPoolFree());
            this.drawLine(renderer, "pool max", WorldRenderer.getPoolMax());
            this.drawLine(renderer, "pool peak", WorldRenderer.getPoolPeak());
        }

        // Mobile platform.
        if (GamePlatform.instance.isMobile()) {
            // HUD
            Hud hud = this.client.hud;
            if (hud != null) {
                this.drawLine(renderer, "joystick", hud.getJoyStick());
            }
        }
    }

    private Vec3i block2sectionPos(BlockPos blockPos) {
        return new Vec3i(blockPos.x() / 16, blockPos.y() / 16, blockPos.z() / 16);
    }

    public void drawLine(Renderer renderer, String name, Object value) {
        this.y += 10;
        renderer.drawText(name + ": " + value, DebugGui.OFFSET, this.y);
    }

    public void drawLine(Renderer renderer, String text) {
        this.y += 10;
        renderer.drawText(text, DebugGui.OFFSET, this.y);
    }
}
