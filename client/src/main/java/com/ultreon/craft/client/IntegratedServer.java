package com.ultreon.craft.client;

import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.server.CommonConstants;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class IntegratedServer extends UltracraftServer {
    private final UltracraftClient client = UltracraftClient.get();
    private boolean openToLan = false;
    private ServerPlayer host;

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
        ServerPlayer player = this.getPlayerByUuid(localPlayer.getUuid());
        try {
            if (player != null && player.getUuid().equals(localPlayer.getUuid()) && this.getStorage().exists("data/player.ubo")) {
                var playerData = this.getStorage().<MapType>read("data/player.ubo");
                player.loadWithPos(playerData);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.host = player;

        if (CommonConstants.INSPECTION_ENABLED) {
            this.node.createNode("host", () -> this);
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
        this.deferWorldLoad(() -> super.placePlayer(player));
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
        return this.host.getUuid();
    }

    public UltracraftClient getClient() {
        return this.client;
    }
}
