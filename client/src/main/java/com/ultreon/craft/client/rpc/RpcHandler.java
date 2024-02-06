package com.ultreon.craft.client.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public interface RpcHandler {
    Iterable<RpcHandler> HANDLERS = ServiceLoader.load(RpcHandler.class);
    Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    void start();

    void setActivity(GameActivity newActivity);

    boolean isEnabled();

    void setEnabled(boolean b);

    boolean isReady();

    boolean isShuttingDown();

    void close();

    static void newActivity(GameActivity newActivity) {
        for (RpcHandler handler : HANDLERS) {
            handler.setActivity(newActivity);
        }
    }

    static void disable() {
        for (RpcHandler handler : HANDLERS) {
            if (handler.isEnabled()) {
                handler.setEnabled(false);
                shutdown();
            }
        }
    }

    static void enable() {
        for (RpcHandler handler : HANDLERS) {
            if (!handler.isEnabled()) {
                handler.setEnabled(true);
                handler.start();
            }
        }
    }

    private static void shutdown() {
        for (RpcHandler handler : HANDLERS) {
            if (!handler.isReady()) {
                RpcHandler.LOGGER.warn("Failed to send RPC shutdown message, not ready!");
                return;
            }

            handler.close();
        }
    }
}
