package com.ultreon.craft.client;

public interface ClientModInit {

    String ENTRYPOINT_KEY = "client-init";

    void onInitializeClient();
}
