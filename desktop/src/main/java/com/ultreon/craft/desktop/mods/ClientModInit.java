package com.ultreon.craft.desktop.mods;

public interface ClientModInit {

    String ENTRYPOINT_KEY = "client-init";

    void onInitializeClient();
}
