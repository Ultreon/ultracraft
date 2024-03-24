package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.ModInit;
import com.ultreon.craft.config.UltracraftServerConfig;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.debug.inspect.InspectionRoot;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.dedicated.gui.DedicatedServerGui;
import com.ultreon.craft.text.LanguageBootstrap;
import com.ultreon.craft.util.ModLoadingContext;
import com.ultreon.libs.datetime.v0.Duration;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
    private static final Object WAITER = new Object();
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
            ModLoadingContext.withinContext(FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow(), () -> {
                try {
                    run();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            });

            // Invoke FabricMC entrypoint for dedicated server.
            FabricLoader loader = FabricLoader.getInstance();
            loader.invokeEntrypoints(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);
            loader.invokeEntrypoints(DedicatedServerModInit.ENTRYPOINT_KEY, DedicatedServerModInit.class, DedicatedServerModInit::onInitialize);

            LanguageBootstrap.bootstrap.set((path, args1) -> server != null ? server.handleTranslation(path, args1) : path);

            Main.serverLoader = new ServerLoader();
            Main.serverLoader.load();

            // Start the server.
            @SuppressWarnings("InstantiationOfUtilityClass") InspectionRoot<Main> inspection = new InspectionRoot<>(new Main());
            Main.server = new DedicatedServer(inspection);
            Main.server.start();

            // Handle server console commands.
            Scanner scanner = new Scanner(System.in);

            SwingUtilities.invokeLater(() -> {
                DedicatedServerGui gui = new DedicatedServerGui();
                gui.setVisible(true);
            });

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

    private static void waitForKey() {
        while (true) {
            try {
                if (System.in.read() != -1) {
                    System.exit(1);
                    break;
                }

                WAITER.wait(50);
            } catch (IOException e) {
                LOGGER.warn("Failed to read from stdin", e);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Failed to sleep", e);
                break;
            }
        }
    }

    private static void run() throws InterruptedException {
        ServerConfig serverConfig = new ServerConfig();
        new UltracraftServerConfig();
        if (!Files.exists(serverConfig.getConfigPath(), LinkOption.NOFOLLOW_LINKS)) {
            serverConfig.save();

            Main.LOGGER.info("First-initialization finished, set up your config in server_config.json5 and restart the server.");
            Main.LOGGER.info("We will wait 10 seconds so you would be able to stop the server for configuration.");

            Thread thread = new Thread(Main::waitForKey);
            thread.start();

            Duration.ofSeconds(10).sleep();

            thread.interrupt();
            thread.join();
        } else {
            serverConfig.load();
        }
    }
}
