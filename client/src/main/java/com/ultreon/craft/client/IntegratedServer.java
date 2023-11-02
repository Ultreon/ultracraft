package com.ultreon.craft.client;

import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.WorldStorage;

import java.io.IOException;
import java.nio.file.Files;

public class IntegratedServer extends UltracraftServer {
    private final UltracraftClient client = UltracraftClient.get();
    private boolean openToLan = false;

    public IntegratedServer(WorldStorage storage) {
        super(storage, null);

        if (Files.notExists(storage.getDirectory())) {
            try {
                storage.createWorld();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    public boolean isOpenToLan() {
        return this.openToLan;
    }

    public void openToLan() {
        this.openToLan = true;
    }

    @Override
    public String toString() {
        return "IntegratedServer{" +
                "openToLan=" + this.openToLan +
                '}';
    }

    public UltracraftClient getClient() {
        return client;
    }
}
