package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.debug.inspect.InspectionRoot;
import com.ultreon.craft.debug.profiler.Profiler;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.text.ServerLanguage;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.WorldStorage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Dedicated server implementation.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class DedicatedServer extends UltracraftServer {
    private static final WorldStorage STORAGE = new WorldStorage(Paths.get("world"));
    private static final Profiler PROFILER = new Profiler();
    @SuppressWarnings("unchecked")
    private final ServerLanguage language = new ServerLanguage(new Locale("en", "us"), CommonConstants.GSON.fromJson(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/assets/ultracraft/languages/en_us.json"))), Map.class), new ElementID("ultracraft"));

    /**
     * Creates a new dedicated server instance.
     *
     * @param host       the hostname for the server.
     * @param port       the port for the server.
     * @param inspection the inspection root.
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
        if (crash.getCrashLog().defaultSave().isFailurePresent()) {
            CommonConstants.LOGGER.error("Failed to save crash log!", crash.getCrashLog().defaultSave().getFailure());
            Runtime.getRuntime().halt(2);
        }

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

    public String handleTranslation(String path, Object[] args1) {
        String translation = language.get(path, args1);
        return translation == null ? path : translation;
    }
}
