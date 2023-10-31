package com.ultreon.craft.server.dedicated;

import com.google.gson.Gson;
import com.ultreon.craft.server.ServerConstants;
import com.ultreon.libs.commons.v0.Identifier;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerMain");
    private static DedicatedServer server;

    public static void main(String[] args) throws IOException, InterruptedException {
        EntrypointUtil.invoke("dedicated-server", DedicatedServerModInit.class, DedicatedServerModInit::onInitialize);

        Identifier.setDefaultNamespace(ServerConstants.NAMESPACE);

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

        ServerConfig config;
        try (var reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, ServerConfig.class);
        }

        Main.server = new DedicatedServer(config);
        Main.server.start();

        Scanner scanner = new Scanner(System.in);

        while(!Main.server.isTerminated()) {
            String commandline = scanner.nextLine();
            if (commandline.equals("stop")) {
                Main.server.shutdown();
                if (!Main.server.awaitTermination(60, TimeUnit.SECONDS)) {
                    Main.server.onTerminationFailed();
                }
            }
        }
    }

    public static DedicatedServer getServer() {
        return Main.server;
    }
}
