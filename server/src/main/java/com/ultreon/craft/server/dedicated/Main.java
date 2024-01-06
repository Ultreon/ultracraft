package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.debug.inspect.InspectionRoot;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.text.LanguageBootstrap;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Dedicated server main class.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@ApiStatus.Internal
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerMain");
    private static DedicatedServer server;
    private static ServerLoader serverLoader;

    /**
     * Main entry point for the server.
     * WARNING: Do not invoke.
     * This will be called by the FabricMC game provider.
     *
     * @param args command line arguments
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the current thread was interrupted
     */
    @ApiStatus.Internal
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            // Invoke FabricMC entrypoint for dedicated server.
            FabricLoader.getInstance().invokeEntrypoints("dedicated-server", DedicatedServerModInit.class, DedicatedServerModInit::onInitialize);

            LanguageBootstrap.bootstrap.set((path, args1) -> server != null ? server.handleTranslation(path, args1) : path);

            Main.serverLoader = new ServerLoader();
            Main.serverLoader.load();

            // First-initialize the server configuration.
            Yaml yaml = new Yaml();
            Path configPath = Path.of("server_config.yml");
            if (Files.notExists(configPath)) {
                Files.writeString(configPath, yaml.dumpAsMap(new ServerConfig()));
                Main.LOGGER.info("First-initialization finished, set up your config in server_config.json and restart the server.");
                Main.LOGGER.info("We will wait 10 seconds so you would be able to stop the server for configuration.");
                Thread.sleep(10000);
            }

            // Read the server configuration file.
            ServerConfig config = yaml.loadAs(Files.readString(configPath), ServerConfig.class);

            // Start the server.
            @SuppressWarnings("InstantiationOfUtilityClass") InspectionRoot<Main> inspection = new InspectionRoot<>(new Main());
            Main.server = new DedicatedServer(config, inspection);
            Main.server.start();

            // Handle server console commands.
            Scanner scanner = new Scanner(System.in);

            while (!Main.server.isTerminated()) {
                // Read command from the server console.
                String commandline = scanner.nextLine();

                if (commandline.equals("stop")) {
                    // Handle stop command.
                    Main.server.shutdown();
                    if (!Main.server.awaitTermination(60, TimeUnit.SECONDS)) {
                        Main.server.onTerminationFailed();
                    }
                }
            }

            UltracraftServer.getWatchManager().stop();
        } catch (ApplicationCrash e) {
            e.getCrashLog().createCrash().printCrash();
        } catch (Exception e) {
            // Server crashed! Saving a crash log, and write it to the server console.
            CrashLog crashLog = new CrashLog("Server crashed!", e);
            crashLog.defaultSave();

            String string = crashLog.toString();
            Main.LOGGER.error(string);
            Runtime.getRuntime().halt(1); //* Halt server since the server crashed.
        }
    }

    /**
     * Gets the server instance.
     * Not recommended to use this method, use {@link UltracraftServer#get()} instead.
     *
     * @return the server instance
     */
    @ApiStatus.Internal
    public static DedicatedServer getServer() {
        return Main.server;
    }
}
