package com.ultreon.craft.server.dedicated;

public interface DedicatedServerModInit {
    String ENTRYPOINT_KEY = "server-init";

    void onInitialize();
}
