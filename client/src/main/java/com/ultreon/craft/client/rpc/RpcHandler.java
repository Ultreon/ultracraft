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
    private static IPCClient client;
    private static OffsetDateTime start;
    private static boolean failed;

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
                }

                @Override
                public void onDisconnect(IPCClient client, Throwable t) {
                    RpcHandler.LOGGER.warn("Discord RPC over IPC disconnected!", t);
                }


            });

            RpcHandler.client.connect();
            Runtime.getRuntime().addShutdownHook(new Thread(RpcHandler.client::close));
        } catch (Exception e) {
            RpcHandler.failed = true;
        }
    }

    public static void setActivity(GameActivity newActivity) {
        if (RpcHandler.failed) {
            return;
        }

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
