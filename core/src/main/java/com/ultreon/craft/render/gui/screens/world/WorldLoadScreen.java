package com.ultreon.craft.render.gui.screens.world;

import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.WorldRenderer;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.WorldInfo;
import com.ultreon.libs.commons.v0.Identifier;

import java.io.IOException;

public class WorldLoadScreen extends Screen {
    private final SavedWorld savedWorld;
    private final WorldInfo info;
    private long nextLog;

    public WorldLoadScreen(SavedWorld savedWorld) {
        super("Loading World");
        this.savedWorld = savedWorld;
        try {
            this.info = this.savedWorld.getWorldInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void show() {
        super.show();

        this.game.world = new World(this.savedWorld, this.info.getSeed());
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

        this.game.respawn();
        UltreonCraft.LOGGER.debug("Player spawned, enabling world rendering now.");
        this.game.worldRenderer = new WorldRenderer(this.game.world);
        this.game.renderWorld = true;
        UltreonCraft.LOGGER.debug("World rendering enabled, closing load screen.");
        this.game.runLater(new Task(new Identifier("world_loaded"), () -> this.game.showScreen(null)));
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        fill(renderer, 0, 0, this.width, this.height, 0xff202020);

        renderer.setColor(Color.rgb(0xffffff));
        renderer.drawCenteredText(this.title, this.width / 2, this.height - this.height / 3);

        World world = this.game.world;
        if (world != null) {
            int chunksToLoad = world.getChunksToLoad();
            if (chunksToLoad != 0) {
                String s = (100 * world.getChunksLoaded() / chunksToLoad) + "%";
                renderer.drawCenteredText(s, this.width / 2, this.height - this.height / 3 - 20);

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

    public WorldInfo getInfo() {
        return this.info;
    }
}
