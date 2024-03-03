package com.ultreon.craft.client.registry;

import com.ultreon.craft.client.InternalApi;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class LanguageRegistry {
    private static final Set<Identifier> REGISTRY = new HashSet<>();

    @InternalApi
    @ApiStatus.Internal
    public static void doRegistration(Consumer<Identifier> consumer) {
        LanguageRegistry.REGISTRY.forEach(consumer);
    }

    public static void register(Identifier id) {
        LanguageRegistry.REGISTRY.add(id);
    }
}
