package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.debug.inspect.InspectionRoot;
import com.ultreon.craft.debug.profiler.Profiler;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.text.ServerLanguage;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.WorldStorage;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

/**
 * Dedicated server implementation.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@SuppressWarnings("GDXJavaStaticResource")
public class DedicatedServer extends UltracraftServer {
    private static final WorldStorage STORAGE = new WorldStorage(Paths.get("world"));
    private static final Profiler PROFILER = new Profiler();
    private final ServerLanguage language = createServerLanguage();

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

    /**
     * Creates a new server language using the specified locale and language map.
     *
     * @return the newly created {@link ServerLanguage} object
     */
    @NotNull
    private ServerLanguage createServerLanguage() {
        // Specify the locale
        Locale locale = Locale.of("en", "us");

        // Load the language resource from the file system
        InputStream resourceAsStream = getClass().getResourceAsStream("/assets/ultracraft/languages/main.json");
        if (resourceAsStream == null) {
            throw new RuntimeException("Could not load language file!");

        }

        // Parse the language resource into a map
        Map<String, String> languageMap;
        try (InputStream resource = resourceAsStream) {
            InputStreamReader json = new InputStreamReader(
                    resource
            );

            //noinspection unchecked
            languageMap = (Map<String, String>) CommonConstants.GSON.fromJson(json, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not load language file!", e);
        }

        // Create and return a new ServerLanguage object
        return new ServerLanguage(locale, languageMap, new Identifier("ultracraft"));
    }

    /**
     * Constructor for the DedicatedServer class.
     *
     * @param inspection the InspectionRoot object for main inspection
     * @throws UnknownHostException if the hostname is unknown
     */
    DedicatedServer(InspectionRoot<Main> inspection) throws UnknownHostException {
        // Call the other constructor with hostname, port, and inspection
        this(ServerConfig.hostname, ServerConfig.port, inspection);

        LOGGER.info("Server started on {}:{}", ServerConfig.hostname, ServerConfig.port);

        try {
            // Create the world storage
            DedicatedServer.STORAGE.createWorld();
        } catch (IOException e) {
            // Throw a RuntimeException if an IOException occurs
            throw new RuntimeException(e);
        }

        // Set the maxPlayers from the config
        this.maxPlayers = ServerConfig.maxPlayers;

        // Set up the spawn for the world
        this.world.setupSpawn();
    }

    /**
     * Dedicated server crash handler.
     *
     * @param crashLog the throwable that caused the crash.
     */
    @Override
    public void crash(CrashLog crashLog) {
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
    public void run() {
        // Get the chunk position of the world's spawn point
        ChunkPos spawnPoint = World.toChunkPos(this.world.getSpawnPoint());

        // Iterate through a 9x9 area around the spawn point
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                // Load the chunk at the offset position from the spawn point
                this.world.loadChunk(spawnPoint.offset(x, z));
            }
        }

        super.run();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will shutdown the server for the dedicated server.
     */
    @Override
    @Blocking
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
    public void fatalCrash(Throwable throwable) {
        this.crash(throwable);
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
