package com.ultreon.craft;

public interface ModInit {

    String ENTRYPOINT_KEY = "init";

    void onInitialize();
}
