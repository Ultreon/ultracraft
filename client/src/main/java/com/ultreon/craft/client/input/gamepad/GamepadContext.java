package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.libs.collections.v0.maps.OrderedHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.Predicate;

public abstract class GamepadContext {
    private static final Map<Predicate<UltracraftClient>, GamepadContext> REGISTRY = new OrderedHashMap<>();
    private static volatile boolean frozen = false;
    public final GamepadMappings mappings = new GamepadMappings();
    private static boolean initialized = false;

    private static boolean isUsingVirtualKeyboard(UltracraftClient client) {
        return UltracraftClient.get().gamepadInput.isVirtualKeyboardOpen();
    }

    protected GamepadContext() {
        if (!initialized) {
            REGISTRY.put(GamepadContext::isUsingVirtualKeyboard, VirtKeyboardGamepadContext.INSTANCE);
            initialized = true;
        }
    }

    public static void register(GamepadContext context, Predicate<UltracraftClient> predicate) {
        if (frozen)
            throw new IllegalStateException("Context registration is frozen.");

        REGISTRY.put(predicate, context);
    }

    @ApiStatus.Internal
    public static void freeze() {
        REGISTRY.put(GamepadContext::isChatting, ChatGamepadContext.INSTANCE);
        REGISTRY.put(GamepadContext::isInGameTargetingBlock, BlockTargetGamepadContext.INSTANCE);
        REGISTRY.put(GamepadContext::isInGame, EntityTargetGamepadContext.INSTANCE);
        REGISTRY.put(GamepadContext::isInCloseableMenu, CloseableMenuGamepadContext.INSTANCE);
        REGISTRY.put(GamepadContext::isInMenu, MenuGamepadContext.INSTANCE);
        REGISTRY.put(Predicate.isEqual(UltracraftClient.get()), new GamepadContext() {

        });

        frozen = true;
    }

    public static boolean isChatting(UltracraftClient client) {
        return client.player != null && client.world != null && client.screen instanceof ChatScreen;
    }

    private static boolean isInGameTargetingEntity(UltracraftClient client) {
//        Crosshair crosshair = Crosshair.get();
//        if (isInGame(client) && crosshair != null) {
//            double entityReach = UltracraftClient.getEntityReach(client.player);
//
//            if (entityReach <= 0) return false;
//            if (crosshair.entity(entityReach) == null) return false;
//            return crosshair.entity(entityReach) instanceof LivingEntity;
//        }
//
//        return false;
        return false;
    }

    private static boolean isInGameTargetingBlock(UltracraftClient client) {
        if (isInGame(client)) {
            return client.cursor != null;
        }
        return false;
    }

    public static boolean isInMenu(UltracraftClient client) {
        return client.screen != null;
    }

    public static boolean isInCloseableMenu(UltracraftClient client) {
        return client.screen != null && client.screen.canCloseWithEsc();
    }

    public static boolean isInGame(UltracraftClient client) {
        return client.player != null && client.screen == null;
    }

    public static GamepadContext get() {
        if (!frozen) return null;
        for (Map.Entry<Predicate<UltracraftClient>, GamepadContext> entry : REGISTRY.entrySet()) {
            if (entry.getKey().test(UltracraftClient.get())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int getYOffset() {
        return 0;
    }

    public int getLeftXOffset() {
        return 0;
    }

    public int getRightXOffset() {
        return 0;
    }
}
