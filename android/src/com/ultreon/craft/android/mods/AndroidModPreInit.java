package com.ultreon.craft.android.mods;

import com.ultreon.craft.AndroidPlatform;

public interface AndroidModPreInit {

    String ENTRYPOINT_KEY = "android-pre-init";

    void onInitializeAndroid(AndroidPlatform launcher);
}
