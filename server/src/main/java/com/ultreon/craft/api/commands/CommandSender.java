package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.text.Formatter;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The CommandSender interface represents an object that can send commands and receive messages.
 * <p>
 * This interface provides methods to get the name, public name (if available), and display name of the command sender.
 * It also allows sending messages to the command sender.
 */
public interface CommandSender {
    @NotNull Location getLocation();

    String getName();
    @Nullable String getPublicName();
    TextObject getDisplayName();

    UUID getUuid();

    /**
     * Sends the command sender a message back.
     *
     * @param message a message that can be formatted by {@link Formatter}.
     */
    void sendMessage(@NotNull String message);

    void sendMessage(@NotNull TextObject component);

    default boolean hasExplicitPermission(@NotNull String permission) {
        return this.hasExplicitPermission(new Permission(permission));
    }

    boolean hasExplicitPermission(@NotNull Permission permission);

    default boolean hasPermission(@NotNull String permission) {
        return this.hasPermission(new Permission(permission));
    }

    default boolean hasPermission(@NotNull Permission permission) {
        UltracraftServer server = UltracraftServer.get();
        if (server != null) {
            server.getDefaultPermissions().has(permission);
        }
        return this.hasExplicitPermission(permission);
    }

    boolean isAdmin();
}
