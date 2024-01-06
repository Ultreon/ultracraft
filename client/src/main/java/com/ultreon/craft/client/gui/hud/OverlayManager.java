package com.ultreon.craft.client.gui.hud;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.util.ElementID;

import java.util.List;

public class OverlayManager {
    private static final OrderedMap<ElementID, HudOverlay> REGISTRY = new OrderedMap<>();

    public static <T extends HudOverlay> T registerTop(ElementID id, T overlay) {
        OverlayManager.REGISTRY.put(OverlayManager.REGISTRY.size(), id, overlay);
        return overlay;
    }

    public static <T extends HudOverlay> T registerAbove(ElementID above, ElementID id, T overlay) {
        int idx = OverlayManager.REGISTRY.indexOf(above);
        OverlayManager.REGISTRY.put(idx + 1, id, overlay);
        return overlay;
    }

    public static <T extends HudOverlay> T registerBelow(ElementID below, ElementID id, T overlay) {
        int idx = OverlayManager.REGISTRY.indexOf(below);
        OverlayManager.REGISTRY.put(idx, id, overlay);
        return overlay;
    }

    public static <T extends HudOverlay> T registerBottom(ElementID id, T overlay) {
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
