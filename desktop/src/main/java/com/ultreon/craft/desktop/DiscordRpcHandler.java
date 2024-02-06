package com.ultreon.craft.desktop;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.ultreon.craft.client.rpc.GameActivity;
import com.ultreon.craft.client.rpc.RpcHandler;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@ApiStatus.Experimental
public class DiscordRpcHandler implements RpcHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("DiscordRpcHandler");
    private static IPCClient client;
    private static OffsetDateTime start;
    private static boolean failed;
    private boolean enabled;
    private boolean ready;
    private boolean shuttingDown;
    private final Thread HOOK = new Thread(this::close);

    public DiscordRpcHandler() {
        failed = false;
        start = OffsetDateTime.now();
        LOGGER.info("Starting Discord RPC handler...");
    }

    @Override
    public void start() {
        try {
            client = new IPCClient(1179421663503323318L);
            Runtime.getRuntime().addShutdownHook(HOOK);
            client.setListener(new IPCListener() {
                @Override
                public void onReady(IPCClient client) {
                    LOGGER.info("Discord RPC over IPC is ready!");

                    RichPresence.Builder builder = new RichPresence.Builder();
                    builder.setState(null)
                            .setDetails("Loading the game!")
                            .setLargeImage("icon")
                            .setStartTimestamp(start);
                    client.sendRichPresence(builder.build());

                    ready = true;
                }

                @Override
                public void onDisconnect(IPCClient client, Throwable t) {
                    ready = false;
                    LOGGER.warn("Discord RPC over IPC disconnected!", t);
                }
            });

            client.connect();
            Runtime.getRuntime().addShutdownHook(new Thread(client::close));
        } catch (Exception e) {
            failed = true;
        }
    }

    public void setActivity(GameActivity newActivity) {
        if (failed) {
            return;
        }

        RichPresence.Builder builder = new RichPresence.Builder();
        String description;
        try {
            description = newActivity.getDescription();
        } catch (Exception e) {
            description = "(\u256F\u00B0\u25A1\u00B0)\u256F\uFE35 \u253B\u2501\u253B";
        }
        if (description == null) {

            builder.setState(null)
                    .setDetails(newActivity.getDisplayName())
                    .setLargeImage("icon")
                    .setStartTimestamp(start);
        } else {
            builder.setState(description)
                    .setDetails(newActivity.getDisplayName())
                    .setLargeImage("icon")
                    .setStartTimestamp(start);
        }

        client.sendRichPresence(builder.build());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean b) {
        enabled = b;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    @Override
    public void close() {
        if (!shuttingDown) {
            shuttingDown = true;
            Runtime.getRuntime().removeShutdownHook(HOOK);
            client.close();
        }
    }
}
