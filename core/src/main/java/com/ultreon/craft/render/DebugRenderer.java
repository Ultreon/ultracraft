package com.ultreon.craft.render;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.util.HitResult;
import com.badlogic.gdx.graphics.Mesh;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.world.ChunkMesh;
import com.ultreon.craft.render.world.WorldRenderer;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.Section;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

public class DebugRenderer {
    private static final int OFFSET = 20;
    private final UltreonCraft game;
    private int y;

    public DebugRenderer(UltreonCraft game) {
        this.game = game;
    }

    public void render(Renderer renderer) {
        this.y = DebugRenderer.OFFSET;

        World world = this.game.world;
        @Nullable WorldRenderer worldRenderer = this.game.worldRenderer;
        if (worldRenderer != null && worldRenderer.isDisposed()) worldRenderer = null;
        if (world != null && world.isDisposed()) world = null;

        this.drawLine(renderer, Mesh.getManagedStatus());

        // World
        if (world != null) {
            this.drawLine(renderer, "fps", Gdx.graphics.getFramesPerSecond());
            // Player
            Player player = this.game.player;
            if (player != null) {
                Vec3i blockPosition = player.blockPosition();
                Vec3i sectionPos = this.block2sectionPos(blockPosition);
                Chunk chunkAt = world.getChunkAt(blockPosition);
                this.drawLine(renderer, "block xyz", blockPosition);
                this.drawLine(renderer, "chunk xyz", sectionPos);
                if (chunkAt != null) {
                    Section sectionAt = chunkAt.getSectionAt(sectionPos.y);
                    this.drawLine(renderer, "chunk render xyz", sectionAt == null ? "null" : sectionAt.renderOffset);
                }
                this.drawLine(renderer, "chunk shown", world.getChunkAt(blockPosition) != null);
                HitResult hitResult = this.game.hitResult;
                if (hitResult != null) this.drawLine(renderer, "break progress", world.getBreakProgress(hitResult.getPos()));
            }

            this.drawLine(renderer, "meshes disposed", ChunkMesh.getMeshesDisposed());
//            this.drawLine(renderer, "meshes built", ChunkMeshBuilder.getMeshesBuilt());
            this.drawLine(renderer, "vertex count", WorldRenderer.getVertexCount());

            if (worldRenderer != null) {
                this.drawLine(renderer, "visible chunks", worldRenderer.getVisibleChunks() + "/" + worldRenderer.getLoadedChunks());
            }

            this.drawLine(renderer, "chunk mesh disposes", WorldRenderer.getChunkMeshFrees());
            this.drawLine(renderer, "chunk loads", World.getChunkLoads());
            this.drawLine(renderer, "chunk unloads", World.getChunkUnloads());
            this.drawLine(renderer, "chunk saves", World.getChunkSaves());
            this.drawLine(renderer, "pool free", WorldRenderer.getPoolFree());
            this.drawLine(renderer, "pool max", WorldRenderer.getPoolMax());
            this.drawLine(renderer, "pool peak", WorldRenderer.getPoolPeak());
        }

        // Mobile platform.
        if (GamePlatform.instance.isMobile()) {
            // HUD
            Hud hud = this.game.hud;
            if (hud != null) {
                this.drawLine(renderer, "joystick", hud.getJoyStick());
            }
        }
    }

    private Vec3i block2sectionPos(Vec3i blockPos) {
        return new Vec3i(blockPos.x / 16, blockPos.y / 16, blockPos.z / 16);
    }

    public void drawLine(Renderer renderer, String name, Object value) {
        this.y += 10;
        renderer.drawText(name + ": " + value, DebugRenderer.OFFSET, this.y);
    }

    public void drawLine(Renderer renderer, String text) {
        this.y += 10;
        renderer.drawText(text, DebugRenderer.OFFSET, this.y);
    }
}
