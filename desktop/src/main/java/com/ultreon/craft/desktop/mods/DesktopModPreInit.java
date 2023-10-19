package com.ultreon.craft.desktop.mods;

import com.ultreon.craft.DesktopPlatform;

public interface DesktopModPreInit {

    String ENTRYPOINT_KEY = "desktop-pre-init";

    void onInitializeDesktop(DesktopPlatform platform);
}
