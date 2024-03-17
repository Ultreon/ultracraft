package com.ultreon.craft.client;

import com.ultreon.craft.client.gui.Notification;
import com.ultreon.craft.client.gui.icon.MessageIcon;
import com.ultreon.craft.client.gui.screens.TitleScreen;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.network.packets.s2c.S2CPlayerSetPosPacket;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class IntegratedServer extends UltracraftServer {
    private final UltracraftClient client = UltracraftClient.get();
    private boolean openToLan = false;
    private @Nullable ServerPlayer host;

    public IntegratedServer(WorldStorage storage) {
        super(storage, UltracraftClient.PROFILER, UltracraftClient.get().inspection);

        if (Files.notExists(storage.getDirectory())) {
            try {
                storage.createWorld();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Loads player data and sets the host player if found.
     *
     * @param localPlayer The local player object to load
     */
    public void loadPlayer(@NotNull LocalPlayer localPlayer) {
        // Retrieve the player object from the server
        ServerPlayer player = this.getPlayer(localPlayer.getUuid());

        try {
            // Check if the player exists on the server and player data file exists
            if (player != null && player.getUuid().equals(localPlayer.getUuid()) && this.getStorage().exists("player.ubo")) {
                // Load player data from storage
                var playerData = this.getStorage().<MapType>read("player.ubo");
                player.loadWithPos(playerData);

                // Send player position update packet to player connection
                player.connection.send(new S2CPlayerSetPosPacket(player.getPosition()));
            } else if (player == null) {
                // Throw exception if player not found
                throw new IllegalStateException("Player not found.");
            }
        } catch (IOException e) {
            // Wrap and rethrow IOException as a RuntimeException
            throw new RuntimeException(e);
        }

        // Set the host player if the player UUID matches the local player UUID
        if (player.getUuid().equals(localPlayer.getUuid())) {
            this.host = player;
        }

        // Create a debug node for host player if inspection is enabled
        if (DebugFlags.INSPECTION_ENABLED.enabled()) {
            this.node.createNode("host", () -> this.host);
        }
    }

    /**
     * Saves the player data to storage.
     */
    public void savePlayer() {
        // Get the player from the host
        ServerPlayer player = this.host;

        try {
            // Check if the player is not null
            if (player != null) {
                // Save the player data to a MapType object
                MapType save = player.save(new MapType());

                // Write the player data to storage
                this.getStorage().write(save, "player.ubo");

                // Log that the player data has been saved
                UltracraftServer.LOGGER.info("Saved local player data.");
            } else {
                // Log an error if player is not found
                UltracraftServer.LOGGER.error("Player not found.");
            }
        } catch (IOException e) {
            // Throw a runtime exception if there is an IOException
            throw new RuntimeException(e);
        }
    }

    @Override
    public void crash(CrashLog crashLog) {
        UltracraftClient.crash(crashLog);
    }

    @Override
    protected void onTerminationFailed() {
        throw new ApplicationCrash(new CrashLog("onTerminationFailed", new Throwable("Failed termination of integrated server.")));
    }

    @Override
    public void placePlayer(ServerPlayer player) {
        this.deferWorldLoad(() -> {
            super.placePlayer(player);

            LocalPlayer localPlayer = this.client.player;
            if (localPlayer != null && player.getUuid().equals(localPlayer.getUuid())) {
                this.client.integratedServer.loadPlayer(localPlayer);
            }
        });
    }

    private void deferWorldLoad(Runnable func) {
        this.client.runInTick(func);
    }

    @Override
    public boolean isIntegrated() {
        return true;
    }

    public boolean isOpenToLan() {
        return this.openToLan;
    }

    @ApiStatus.Experimental
    public void openToLan() {
        this.openToLan = true;
    }

    @Override
    public void save(boolean silent) throws IOException {
        super.save(silent);

        try {
            this.savePlayer();
        } catch (Exception e) {
            UltracraftServer.LOGGER.error("Failed to save local player data.", e);
        }
    }

    @Override
    public String toString() {
        return "IntegratedServer{" +
                "openToLan=" + this.openToLan +
                '}';
    }

    @Override
    public void shutdown() {
        super.shutdown();

        this.client.integratedServer = null;
        this.client.showScreen(new TitleScreen());
    }

    @Override
    public void close() {
        super.close();

        this.getConnections().stop();
    }

    @Override
    protected void runTick() {
        this.client.pollServerTick();

        super.runTick();
    }

    @Override
    public UUID getHost() {
        return this.host != null ? this.host.getUuid() : null;
    }

    @Override
    @InternalApi
    @ApiStatus.Internal
    public void handleWorldSaveError(Exception e) {
        super.handleWorldSaveError(e);

        this.client.notifications.add(Notification.builder("Error", "Failed to save world.")
                .subText("Auto Save")
                .icon(MessageIcon.ERROR)
                .build());
    }

    @Override
    public void handleChunkLoadFailure(ChunkPos globalPos, String reason) {
        super.handleChunkLoadFailure(globalPos, reason);

        this.client.notifications.add(Notification.builder("Failed to load: " + globalPos.toString(), reason)
                .subText("Chunk Loader")
                .icon(MessageIcon.WARNING)
                .build());
    }

    public UltracraftClient getClient() {
        return this.client;
    }
}
