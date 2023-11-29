package com.ultreon.craft.client.rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@ApiStatus.Experimental
public class RpcHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("RpcHandler");
    private static Thread thread;
    private static final AtomicReference<GameActivity> currentActivity = new AtomicReference<>();
    private static IPCClient client;
    private static OffsetDateTime start;

    public static void start(File sdkPath) {
        RpcHandler.client = new IPCClient(1179421663503323318L);
        RpcHandler.start = OffsetDateTime.now();
        RpcHandler.client.setListener(new IPCListener() {
            @Override
            public void onReady(IPCClient client) {
                RichPresence.Builder builder = new RichPresence.Builder();
                builder.setState(null)
                        .setDetails("Loading the game!")
                        .setLargeImage("icon")
                        .setStartTimestamp(RpcHandler.start);
                client.sendRichPresence(builder.build());
            }
        });
        try {
            RpcHandler.client.connect();
        } catch (NoDiscordClientException e) {
            return;
        }
//        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
    }

    public static void setActivity(GameActivity newActivity) {
        RichPresence.Builder builder = new RichPresence.Builder();
        String description = null;
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
}
