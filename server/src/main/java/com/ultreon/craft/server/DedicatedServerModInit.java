package com.ultreon.craft.server;

public interface DedicatedServerModInit {

    String ENTRYPOINT_KEY = "server-init";

    void onInitialize();
}
