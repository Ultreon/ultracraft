package com.ultreon.craft.server.player;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Base class for cached players and server players.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public sealed interface CacheablePlayer permits CachedPlayer, ServerPlayer {
    String getName();

    @Nullable UUID getUuid();

    boolean isOnline();

    boolean isCache();
}
