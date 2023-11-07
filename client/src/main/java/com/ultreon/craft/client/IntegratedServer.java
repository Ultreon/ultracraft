package com.ultreon.craft.client;

import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.network.packets.s2c.S2CPlayerSetPosPacket;
import com.ultreon.craft.server.CommonConstants;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
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

    public void loadPlayer(@NotNull LocalPlayer localPlayer) {
        ServerPlayer player = this.getPlayer(localPlayer.getUuid());
        try {
            if (player != null && player.getUuid().equals(localPlayer.getUuid()) && this.getStorage().exists("player.ubo")) {
                var playerData = this.getStorage().<MapType>read("player.ubo");
                player.loadWithPos(playerData);
                player.connection.send(new S2CPlayerSetPosPacket(player.getPosition()));
            } else if (player == null) {
                throw new IllegalStateException("Player not found.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (player.getUuid().equals(localPlayer.getUuid())) {
            this.host = player;
        }

        if (CommonConstants.INSPECTION_ENABLED) {
            this.node.createNode("host", () -> this.host);
        }
    }

    public void savePlayer() {
        ServerPlayer player = this.host;
        try {
            if (player != null) {
                MapType save = player.save(new MapType());
                this.getStorage().write(save, "player.ubo");
                UltracraftServer.LOGGER.info("Saved local player data.");
            } else {
                UltracraftServer.LOGGER.error("Player not found.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void crash(Throwable t) {
        UltracraftClient.crash(t);
    }

    @Override
    protected void onTerminationFailed() {
        throw new Error("Failed termination of integrated server.");
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
    public void load() throws IOException {
        super.load();
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
    protected void runTick() {
        this.client.pollServerTick();

        super.runTick();
    }

    @Override
    public UUID getHost() {
        return this.host != null ? this.host.getUuid() : null;
    }

    public UltracraftClient getClient() {
        return this.client;
    }
}
