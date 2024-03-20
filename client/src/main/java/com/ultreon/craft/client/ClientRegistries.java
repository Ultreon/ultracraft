package com.ultreon.craft.client;

import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.debug.DebugPage;
import com.ultreon.craft.client.render.RenderType;
import com.ultreon.craft.registry.Registry;

/**
 * A class that contains registries for the client.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class ClientRegistries {
    // Registry for RenderType
    public static final Registry<RenderType> RENDER_TYPE = ClientRegistries.<RenderType>builder("render_type").build();

    // Registry for Font
    public static final Registry<Font> FONT = ClientRegistries.<Font>builder("font").build();

    // Registry for DebugPage
    public static final Registry<DebugPage> DEBUG_PAGE = ClientRegistries.<DebugPage>builder("debug_page").build();

    /**
     * Creates a Registry builder with the specified name and type getter.
     *
     * @param name       The name of the registry.
     * @param typeGetter The type getter for the registry.
     * @param <T>        The type of the registry.
     * @return The Registry builder.
     */
    private static <T> Registry.Builder<T> builder(String name, T... typeGetter) {
        return Registry.builder(name, typeGetter).doNotSync();
    }
}
