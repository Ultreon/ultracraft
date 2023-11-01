package com.ultreon.craft.server.dedicated;

import com.google.gson.Gson;
import com.ultreon.craft.server.ServerConstants;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.crash.v0.CrashLog;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Dedicated server main class.
 *
 * @author XyperCode
 * @since 0.1.0
 */
@ApiStatus.Internal
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerMain");
    private static DedicatedServer server;

    @ApiStatus.Internal
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            // Invoke QuiltMC entrypoint for dedicated server.
            EntrypointUtil.invoke("dedicated-server", DedicatedServerModInit.class, DedicatedServerModInit::onInitialize);

            // Set default namespace in CoreLibs identifier.
            Identifier.setDefaultNamespace(ServerConstants.NAMESPACE);

            // First-initialize the server configuration.
            Gson gson = new Gson();
            File configFile = new File("server_config.json");
            if (!configFile.exists()) {
                try (var writer = new FileWriter(configFile)) {
                    gson.toJson(new ServerConfig(), ServerConfig.class, writer);
                }

                Main.LOGGER.info("First-initialization finished, set up your config in server_config.json and restart the server.");
                Main.LOGGER.info("We will wait 10 seconds so you would be able to stop the server for configuration.");
                Thread.sleep(10000);
            }

            // Read the server configuration file.
            ServerConfig config;
            try (var reader = new FileReader(configFile)) {
                config = gson.fromJson(reader, ServerConfig.class);
            }

            // Start the server.
            Main.server = new DedicatedServer(config);
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
        } catch (Throwable t) {
            // Server crashed! Saving a crash log, and write it to the server console.
            CrashLog crashLog = new CrashLog("Server crashed!", t);
            crashLog.defaultSave();

            String string = crashLog.toString();
            Main.LOGGER.error(string);
        }
    }

    @ApiStatus.Internal
    public static DedicatedServer getServer() {
        return Main.server;
    }
}
