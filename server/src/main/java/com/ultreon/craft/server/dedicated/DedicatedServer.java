package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.AES256;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.libs.crash.v0.ApplicationCrash;
import com.ultreon.libs.crash.v0.CrashLog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Dedicated server implementation.
 *
 * @author XyperCode
 * @since 0.1.0
 */
public class DedicatedServer extends UltracraftServer {
    private static final WorldStorage STORAGE = new WorldStorage(Paths.get("world"));
    private final String password;

    /**
     * Creates a new dedicated server instance.
     *
     * @param host the hostname for the server.
     * @param port the port for the server.
     * @throws UnknownHostException if the hostname cannot be resolved.
     * @deprecated Use {@link #DedicatedServer(String, int, String)} instead.
     */
    @Deprecated(forRemoval = true)
    public DedicatedServer(String host, int port) throws UnknownHostException, NoSuchAlgorithmException, InvalidKeySpecException {
        this(host, port, "Welcome123");
    }

    /**
     * Creates a new dedicated server instance.
     *
     * @param host the hostname for the server.
     * @param port the port for the server.
     * @param password the password of the server. Used to authenticate to the server.
     * @throws UnknownHostException if the hostname cannot be resolved.
     */
    public DedicatedServer(String host, int port, String password) throws UnknownHostException, NoSuchAlgorithmException, InvalidKeySpecException {
        super(DedicatedServer.STORAGE, AES256.createKey(password));

        if (password == null || password.isEmpty()) {
            UltracraftServer.LOGGER.error("The password cannot be null or empty!");
            Runtime.getRuntime().halt(1);
        }

        if (password.equals("<PASSWORD>")) {
            UltracraftServer.LOGGER.error("The password cannot be the default password!");
            Runtime.getRuntime().halt(1);
        }

        this.password = password;

        this.getConnections().startTcpServer(InetAddress.getByName(host), port);
    }

    DedicatedServer(ServerConfig config) throws UnknownHostException, NoSuchAlgorithmException, InvalidKeySpecException {
        this(config.hostname, config.port, config.password);

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

    /**
     * Ssssh secret 🤫
     *
     * @return the password of the server.
     */
    public String getPassword() {
        return this.password;
    }
}
