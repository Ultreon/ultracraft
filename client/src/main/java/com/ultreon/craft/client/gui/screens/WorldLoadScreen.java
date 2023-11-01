package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.craft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;

public class WorldLoadScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldLoadScreen.class);
    private final WorldStorage storage;
    private long nextLog;
    private ServerWorld world;
    private DeathScreen closeScreen;
    private String message = "Loading world...";

    public WorldLoadScreen(WorldStorage storage) {
        super("Loading World");
        this.storage = storage;
    }

    @Override
    public void init() {
        super.init();

        IntegratedServer server = new IntegratedServer(this.storage);
        this.client.integratedServer = server;
        this.world = server.getWorld();
        new Thread(this::run, "World Loading").start();
    }

    @Override
    public boolean close(@Nullable Screen to) {
        DeathScreen closeScreen = this.closeScreen;
        if (to == null && closeScreen != null) {
            this.client.showScreen(closeScreen);
            return false;
        }
        return super.close(to);
    }

    public void run() {
        try {
            assert this.world != null;
            MathUtils.random.setSeed(this.world.getSeed());

            this.message("Starting integrated server..");
            this.client.startIntegratedServer();

            WorldLoadScreen.LOGGER.debug("Loading world...");
            try {
                this.world.load();
                WorldLoadScreen.LOGGER.debug("World loaded!");
            } catch (IOException e) {
                UltracraftClient.crash(e);
                return;
            }

            WorldLoadScreen.LOGGER.debug("Set spawn point");

            this.world.setupSpawn();

            UltracraftServer.invoke(() -> {
                try {
                    WorldLoadScreen.LOGGER.info("Loading spawn chunks...");

                    ChunkPos spawnChunk = World.toChunkPos(this.world.getSpawnPoint());
                    int spawnChunkX = spawnChunk.x();
                    int spawnChunkZ = spawnChunk.z();

                    for (int chunkX = spawnChunkX - 1; chunkX <= spawnChunkX + 1; chunkX++) {
                        for (int chunkZ = spawnChunkZ - 1; chunkZ <= spawnChunkZ + 1; chunkZ++) {
                            this.world.loadChunk(spawnChunkX, spawnChunkZ);
                        }
                    }

                    WorldLoadScreen.LOGGER.info("Spawn chunks loaded!");
                } catch (Throwable t) {
                    if (t instanceof Error) {
                        UltracraftClient.crash(t);
                        return;
                    }
                    WorldLoadScreen.LOGGER.error("Failed to load chunks for world.", t);
                }
            }).join();

            UltracraftClient.LOGGER.debug("Player spawned, enabling world rendering now.");
        } catch (Throwable throwable) {
            UltracraftClient.LOGGER.error("Failed to load world:", throwable);
            UltracraftClient.crash(throwable);
        }
    }

    private void message(String message) {
        WorldLoadScreen.LOGGER.debug(message);
        this.message = message;
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        GuiComponent.fill(renderer, 0, 0, this.width, this.height, 0xff202020);

        renderer.setColor(Color.rgb(0xffffff));
        renderer.drawCenteredText(this.title, this.width / 2, this.height / 3);

        ServerWorld world = this.world;
        if (world != null) {
            int chunksToLoad = world.getChunksToLoad();
            if (chunksToLoad != 0) {
                String s = (100 * world.getChunksLoaded() / chunksToLoad) + "%";
                renderer.drawCenteredText(this.message, this.width / 2, this.height / 3 + 20);

                if (this.nextLog <= System.currentTimeMillis()) {
                    this.nextLog = System.currentTimeMillis() + 1000;
                    UltracraftClient.LOGGER.info(World.MARKER, "Loading world: " + s);
                }
            }
        }
    }

    @Override
    public boolean canClose() {
        return false;
    }

    public void setCloseScreen(DeathScreen screen) {
        this.closeScreen = screen;
    }
}
