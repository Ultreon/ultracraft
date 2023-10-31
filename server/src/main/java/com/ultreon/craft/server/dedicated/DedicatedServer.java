package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.libs.crash.v0.CrashLog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;

public class DedicatedServer extends UltracraftServer {
    private static final WorldStorage STORAGE = new WorldStorage(Paths.get("world"));

    public DedicatedServer(String host, int port) throws UnknownHostException {
        super(DedicatedServer.STORAGE);

        this.getConnection().startTcpServer(InetAddress.getByName(host), port);
    }

    DedicatedServer(ServerConfig config) throws UnknownHostException {
        this(config.hostname, config.port);

        try {
            DedicatedServer.STORAGE.createWorld();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.maxPlayers = config.maxPlayers;
    }

    @Override
    public void crash(Throwable t) {
        CrashLog crashLog = new CrashLog("Server crashed! :(", t);
        this.world.fillCrashInfo(crashLog);
        crashLog.createCrash();
    }

    @Override
    protected void onTerminationFailed() {
        this.crash(new Error("Termination failed!"));
        Runtime.getRuntime().halt(1);
    }

    @Override
    public boolean isTerminated() {
        return super.isTerminated() && !this.running && UltracraftServer.get() == null;
    }
}
