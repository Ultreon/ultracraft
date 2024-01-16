package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class WorldLoadScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldLoadScreen.class);
    private Label titleLabel;
    private Label descriptionLabel;
    private Label subTitleLabel;
    private final WorldStorage storage;
    private long nextLog;
    private ServerWorld world;
    private DeathScreen closeScreen;
    private volatile int percent = -1;

    public WorldLoadScreen(WorldStorage storage) {
        super(TextObject.translation("ultracraft.screen.world_load"));
        this.storage = storage;
    }

    @Override
    public void build(GuiBuilder builder) {
        IntegratedServer server = new IntegratedServer(this.storage);
        this.client.integratedServer = server;
        this.world = server.getWorld();

        this.titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 - 25))
                .scale(2));

        this.descriptionLabel = builder.add(Label.of("Preparing")
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 + 3)));

        this.subTitleLabel = builder.add(Label.of()
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 + 31)));

        new Thread(this::run, "World Loading").start();
    }

    @Override
    public boolean onClose(@Nullable Screen next) {
        if (!this.client.renderWorld) return false;

        DeathScreen closeScreen = this.closeScreen;
        if (next == null && closeScreen != null) {
            this.client.showScreen(closeScreen);
            return false;
        }
        return super.onClose(next);
    }

    public void run() {
        try {
            assert this.world != null;
            MathUtils.random.setSeed(this.world.getSeed());

            this.message("Starting integrated server..");
            this.client.startIntegratedServer();

            this.message("Loading saved world...");
            if (this.loadGeneric()) return;

            this.message("Set spawn point");

            this.world.setupSpawn();

            UltracraftServer.invokeAndWait(this::loadWithinServer);

            this.message("Waiting for server to finalize...");
        } catch (Exception throwable) {
            UltracraftClient.LOGGER.error("Failed to load world:", throwable);
            UltracraftClient.crash(throwable);
        }
    }

    private boolean loadGeneric() {
        try {
            this.client.integratedServer.load();
            this.message("Saved world loaded!");
        } catch (IOException e) {
            UltracraftClient.crash(e);
            return true;
        }
        return false;
    }

    private void message(String message) {
        this.descriptionLabel.text().setRaw(message);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        renderer.fill(0, 0, this.size.width, this.size.height, Color.rgb(0x202020));

        ServerWorld world = this.world;
        if (world != null && percent >= 0) {
            String percentMsg = percent + "%";
            this.subTitleLabel.text().setRaw(percentMsg);

            if (this.nextLog <= System.currentTimeMillis()) {
                this.nextLog = System.currentTimeMillis() + 1000;
                UltracraftClient.LOGGER.info(World.MARKER, "Loading world: {}", percentMsg);
            }
        }
        this.subTitleLabel.text().setRaw("");
    }

    @Override
    public boolean canClose() {
        return this.client.renderWorld;
    }

    public void setCloseScreen(DeathScreen screen) {
        this.closeScreen = screen;
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }

    public Label getDescriptionLabel() {
        return this.descriptionLabel;
    }

    private void loadWithinServer() {
        try {
            WorldLoadScreen.LOGGER.info("Loading spawn chunks...");

            ChunkPos spawnChunk = World.toChunkPos(this.world.getSpawnPoint());
            int spawnChunkX = spawnChunk.x();
            int spawnChunkZ = spawnChunk.z();

            for (int chunkX = spawnChunkX - 1; chunkX <= spawnChunkX + 1; chunkX++) {
                for (int chunkZ = spawnChunkZ - 1; chunkZ <= spawnChunkZ + 1; chunkZ++) {
                    this.world.loadChunk(spawnChunkX, spawnChunkZ);
                    this.message("Loading spawn chunk " + chunkX + ", " + chunkZ);
                }
            }

            ChunkRefresher refresher = new ChunkRefresher();
            ServerPlayer.refreshChunks(refresher, this.client.integratedServer, this.world, spawnChunk, ListOrderedSet.listOrderedSet(this.world.getChunksAround(this.world.getSpawnPoint().vec().d().add(0.5, 0, 0.5)).stream().filter(chunkPos -> {
                Vec2i loadChunk = new Vec2i(chunkPos.x(), chunkPos.z());
                Vec2i spawnChunkXZ = new Vec2i(spawnChunkX, spawnChunkZ);
                return loadChunk.dst(spawnChunkXZ) < this.client.settings.renderDistance.get();
            }).sorted(Comparator.naturalOrder()).collect(() -> new ArrayList<>(), ArrayList::add, ArrayList::addAll)), new ListOrderedSet<>());
            this.world.setLoader(this::onLoadUpdate);
            this.world.setOnLoaded(this::onLoaded);
            this.world.doRefresh(refresher);
        } catch (Exception t) {
            UltracraftClient.LOGGER.error("Failed to load chunks for world.", t);
        }
    }

    private void onLoadUpdate(String message, float v) {
        this.message(message);
        this.percent = (int) (100 * v);
    }

    private void onLoaded() {
        this.client.showScreen(null);
    }
}
