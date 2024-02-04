package com.ultreon.craft.client.rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@ApiStatus.Experimental
public class RpcHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("RpcHandler");
    private static boolean shuttingDown;
    private static final Thread HOOK = new Thread(() -> {
        shuttingDown = true;
        shutdown();
    });
    private static IPCClient client;
    private static OffsetDateTime start;
    private static boolean failed;
    private static boolean ready = false;
    private static boolean enabled = false;

    public static void start() {
        try {
            RpcHandler.client = new IPCClient(1179421663503323318L);
            RpcHandler.start = OffsetDateTime.now();
            RpcHandler.client.setListener(new IPCListener() {
                @Override
                public void onReady(IPCClient client) {
                    RpcHandler.LOGGER.info("Discord RPC over IPC is ready!");

                    RichPresence.Builder builder = new RichPresence.Builder();
                    builder.setState(null)
                            .setDetails("Loading the game!")
                            .setLargeImage("icon")
                            .setStartTimestamp(RpcHandler.start);
                    client.sendRichPresence(builder.build());

                    RpcHandler.ready = true;
                }

                @Override
                public void onDisconnect(IPCClient client, Throwable t) {
                    RpcHandler.failed = true;
                    RpcHandler.ready = false;
                    RpcHandler.LOGGER.warn("Discord RPC over IPC disconnected!", t);
                }
            });

            RpcHandler.client.connect();
            Runtime.getRuntime().addShutdownHook(HOOK);
        } catch (Exception e) {
            RpcHandler.failed = true;
        }
    }

    public static void setActivity(GameActivity newActivity) {
        if (RpcHandler.failed || !RpcHandler.ready) {
            return;
        }

        RichPresence.Builder builder = new RichPresence.Builder();
        String description;
        try {
            description = newActivity.getDescription();
        } catch (Exception e) {
            description = "(╯°□°)╯︵ ┻━┻";
        }
        if (description == null) {

            builder.setState(null)
                    .setDetails(newActivity.getDisplayName())
                    .setLargeImage("icon")
                    .setStartTimestamp(RpcHandler.start);
        } else {
            builder.setState(description)
                    .setDetails(newActivity.getDisplayName())
                    .setLargeImage("icon")
                    .setStartTimestamp(RpcHandler.start);
        }

        RpcHandler.client.sendRichPresence(builder.build());
    }

    public static void disable() {
        if (RpcHandler.enabled) {
            RpcHandler.enabled = false;
            shutdown();
        }
    }

    public static void enable() {
        if (!RpcHandler.enabled) {
            RpcHandler.enabled = true;
            start();
        }
    }

    private static void shutdown() {
        if (!ready) {
            RpcHandler.LOGGER.warn("Failed to send RPC shutdown message, not ready!");
            return;
        }

        if (!RpcHandler.shuttingDown) {
            Runtime.getRuntime().removeShutdownHook(RpcHandler.HOOK);
        }
        RpcHandler.client.close();
    }
}
