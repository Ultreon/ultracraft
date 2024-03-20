package com.ultreon.craft.client.api.events;

import com.ultreon.craft.client.GameRenderer;
import com.ultreon.craft.client.gui.Hud;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.world.World;

public class RenderEvents {
    public static final Event<RenderScreen> PRE_RENDER_SCREEN = Event.withValue();
    public static final Event<RenderScreen> POST_RENDER_SCREEN = Event.withValue();

    public static final Event<RenderWorld> PRE_RENDER_WORLD = Event.withValue();
    public static final Event<RenderWorld> POST_RENDER_WORLD = Event.withValue();

    public static final Event<RenderGame> PRE_RENDER_GAME = Event.withValue();
    public static final Event<RenderGame> POST_RENDER_GAME = Event.withValue();

    public static final Event<RenderHud> RENDER_HUD = Event.withValue();

    @FunctionalInterface
    public interface RenderScreen {
        void onRenderScreen(Screen screen, Renderer renderer, float x, float y, float deltaTime);
    }

    @FunctionalInterface
    public interface RenderWorld {
        void onRenderWorld(World world, WorldRenderer worldRenderer);
    }

    @FunctionalInterface
    public interface RenderGame {
        void onRenderGame(GameRenderer gameRenderer, Renderer renderer, float deltaTime);
    }

    @FunctionalInterface
    public interface RenderHud {
        void onRenderHud(Hud hud, Renderer renderer, float deltaTime);
    }
}
