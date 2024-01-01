package com.ultreon.craft.client.gui.hud;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.List;

public class OverlayManager {
    private static final OrderedMap<Identifier, HudOverlay> REGISTRY = new OrderedMap<>();

    public static <T extends HudOverlay> T registerTop(Identifier id, T overlay) {
        OverlayManager.REGISTRY.put(OverlayManager.REGISTRY.size(), id, overlay);
        return overlay;
    }

    public static <T extends HudOverlay> T registerAbove(Identifier above, Identifier id, T overlay) {
        int idx = OverlayManager.REGISTRY.indexOf(above);
        OverlayManager.REGISTRY.put(idx + 1, id, overlay);
        return overlay;
    }

    public static <T extends HudOverlay> T registerBelow(Identifier below, Identifier id, T overlay) {
        int idx = OverlayManager.REGISTRY.indexOf(below);
        OverlayManager.REGISTRY.put(idx, id, overlay);
        return overlay;
    }

    public static <T extends HudOverlay> T registerBottom(Identifier id, T overlay) {
        OverlayManager.REGISTRY.put(0, id, overlay);
        return overlay;
    }

    public static List<HudOverlay> getOverlays() {
        return OverlayManager.REGISTRY.valueList();
    }

    public static void render(Renderer renderer, float deltaTime) {
        for (HudOverlay overlay : OverlayManager.getOverlays()) {
            overlay.render(renderer, deltaTime);
        }
    }
}
