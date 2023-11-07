package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.debug.Profiler;
import com.ultreon.craft.debug.inspect.InspectionRoot;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.libs.crash.v0.ApplicationCrash;
import com.ultreon.libs.crash.v0.CrashLog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;

/**
 * Dedicated server implementation.
 *
 * @author XyperCode
 * @since 0.1.0
 */
public class DedicatedServer extends UltracraftServer {
    private static final WorldStorage STORAGE = new WorldStorage(Paths.get("world"));
    private static final Profiler PROFILER = new Profiler();

    /**
     * Creates a new dedicated server instance.
     *
     * @param host       the hostname for the server.
     * @param port       the port for the server.
     * @param inspection
     * @throws UnknownHostException if the hostname cannot be resolved.
     */
    public DedicatedServer(String host, int port, InspectionRoot<Main> inspection) throws UnknownHostException {
        super(DedicatedServer.STORAGE, DedicatedServer.PROFILER, inspection);

        this.getConnections().startTcpServer(InetAddress.getByName(host), port);
    }

    DedicatedServer(ServerConfig config, InspectionRoot<Main> inspection) throws UnknownHostException {
        this(config.hostname, config.port, inspection);

        try {
            DedicatedServer.STORAGE.createWorld();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.maxPlayers = config.maxPlayers;

        this.world.setupSpawn();
    }

    /**
     * Dedicated server crash handler.
     *
     * @param t the throwable that caused the crash.
     */
    @Override
    public void crash(Throwable t) {
        // Create crash log.
        CrashLog crashLog = new CrashLog("Server crashed! :(", t);
        this.world.fillCrashInfo(crashLog);
        ApplicationCrash crash = crashLog.createCrash();

        // Print and save the crash log.
        crash.printCrash();
        crash.getCrashLog().defaultSave();

        Runtime.getRuntime().halt(1); //* Halt server since the server crashed.
    }

    @Override
    public void shutdown() {
        super.shutdown();

        this.profiler.dispose();
    }

    /**
     * {@inheritDoc}
     * This will crash and halt the server for the dedicated server.
     */
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
