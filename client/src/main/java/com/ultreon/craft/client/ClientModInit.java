package com.ultreon.craft.client;

/**
 * Interface for client-side mod initialization.
 */
public interface ClientModInit {

    /**
     * Key for the client-side initialization entry point.
     */
    String ENTRYPOINT_KEY = "client-init";

    /**
     * Called when initializing the client-side mod.
     */
    void onInitializeClient();
}
