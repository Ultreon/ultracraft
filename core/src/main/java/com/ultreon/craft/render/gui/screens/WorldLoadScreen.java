package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.world.WorldRenderer;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.Identifier;

import java.io.IOException;

public class WorldLoadScreen extends Screen {
    private final SavedWorld savedWorld;
    private long nextLog;

    public WorldLoadScreen(SavedWorld savedWorld) {
        super("Loading World");
        this.savedWorld = savedWorld;
    }

    @Override
    public void show() {
        super.show();

        this.game.world = new World(this.savedWorld, 16, 16);
        new Thread(this::run, "World Loading").start();
    }

    public void run() {
        assert this.game.world != null;
        MathUtils.random.setSeed(this.game.world.getSeed());

        try {
            this.game.world.load();
        } catch (IOException e) {
            UltreonCraft.crash(e);
            return;
        }

        int spawnChunkX = MathUtils.random(-32, 31);
        int spawnChunkZ = MathUtils.random(-32, 31);
        int spawnX = MathUtils.random(spawnChunkX * 16, spawnChunkX * 16 + 15);
        int spawnZ = MathUtils.random(spawnChunkX * 16, spawnChunkX * 16 + 15);

        this.game.world.setSpawnPoint(spawnX, spawnZ);

        for (int chunkX = spawnChunkX - 1; chunkX <= spawnChunkX + 1; chunkX++) {
            for (int chunkZ = spawnChunkZ - 1; chunkZ <= spawnChunkZ + 1; chunkZ++) {
                this.game.world.loadChunk(spawnChunkX, spawnChunkZ);
            }
        }

        this.game.respawnAsync().thenRun(() -> {
            UltreonCraft.LOGGER.debug("Player spawned, enabling world rendering now.");
            this.startRenderingWorld();
            UltreonCraft.LOGGER.debug("World rendering enabled, closing load screen.");
            this.game.submit(new Task<>(new Identifier("world_loaded"), () -> this.game.showScreen(null)));
        });
    }

    private void startRenderingWorld() {
        if (!UltreonCraft.isOnRenderingThread()) {
            UltreonCraft.invokeAndWait(this::startRenderingWorld);
            return;
        }

        this.game.worldRenderer = new WorldRenderer(this.game.world, this.game.modelBatch);
        this.game.renderWorld = true;
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        fill(renderer, 0, 0, this.width, this.height, 0xff202020);

        renderer.setColor(Color.rgb(0xffffff));
        renderer.drawCenteredText(this.title, this.width / 2, this.height / 3);

        World world = this.game.world;
        if (world != null) {
            int chunksToLoad = world.getChunksToLoad();
            if (chunksToLoad != 0) {
                String s = (100 * world.getChunksLoaded() / chunksToLoad) + "%";
                renderer.drawCenteredText(s, this.width / 2, this.height / 3 - 20);

                if (this.nextLog <= System.currentTimeMillis()) {
                    this.nextLog = System.currentTimeMillis() + 1000;
                    UltreonCraft.LOGGER.info(World.MARKER, "Loading world: " + s);
                }
            }
        }
    }

    @Override
    public boolean canClose() {
        return false;
    }
}
