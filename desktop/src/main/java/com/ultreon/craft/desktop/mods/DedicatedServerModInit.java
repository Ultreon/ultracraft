package com.ultreon.craft.desktop.mods;

public interface DedicatedServerModInit {

    String ENTRYPOINT_KEY = "server-init";

    void onInitialize();
}
