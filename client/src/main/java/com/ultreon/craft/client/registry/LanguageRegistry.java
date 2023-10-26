package com.ultreon.craft.client.registry;

import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class LanguageRegistry {
    private static final Set<Identifier> REGISTRY = new HashSet<>();

    @ApiStatus.Internal
    public static void doRegistration(Consumer<Identifier> consumer) {
        REGISTRY.forEach(consumer);
    }

    public static void register(Identifier id) {
        REGISTRY.add(id);
    }
}
