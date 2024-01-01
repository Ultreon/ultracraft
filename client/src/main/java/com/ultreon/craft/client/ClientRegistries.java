package com.ultreon.craft.client;

import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.debug.DebugPage;
import com.ultreon.craft.client.render.RenderType;
import com.ultreon.craft.registry.Registry;

public class ClientRegistries {
    public static final Registry<RenderType> RENDER_TYPE = ClientRegistries.<RenderType>builder("render_type").build();
    public static final Registry<Font> FONT = ClientRegistries.<Font>builder("font").build();
    public static final Registry<DebugPage> DEBUG_PAGE = ClientRegistries.<DebugPage>builder("debug_page").build();

    private static <T> Registry.Builder<T> builder(String name, T... typeGetter) {
        return Registry.builder(name, typeGetter).doNotSync();
    }
}
