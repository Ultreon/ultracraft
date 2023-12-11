package com.ultreon.craft.client.init;

import com.ultreon.craft.client.gui.hud.ChatOverlay;
import com.ultreon.craft.client.gui.hud.OverlayManager;
import com.ultreon.libs.commons.v0.Identifier;

public class Overlays {
    public static final ChatOverlay CHAT = OverlayManager.registerTop(new Identifier("chat"), new ChatOverlay());

    public static void nopInit() {

    }
}
