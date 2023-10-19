package com.ultreon.craft.desktop.mods;

public interface ModInit {

    String ENTRYPOINT_KEY = "init";

    void onInitialize();
}
