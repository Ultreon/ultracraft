package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.registry.CommandRegistry;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.chat.Chat;
import com.ultreon.craft.text.Formatter;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The CommandSender interface represents an object that can send commands and receive messages.
 * <p>
 * This interface provides methods to get the name, public name (if available), and display name of the command sender.
 * It also allows sending messages to the command sender and executing commands with it.
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

    /**
     * Executes a slash command with the given input.
     * The method expects to have the first slash pre-removed from the input.
     * <p>
     * So, if the actual command is "/example", the method should be called with "example" as the input.<br>
     * And if the actual command is "//example", the method should be called with "/example" as the input.
     * <p>
     * This method doesn't need to be implemented. As it executes the command already by default.
     *
     * @param input The input string containing the command and arguments.
     */
    default void execute(String input) {
        // Trim the input to remove any leading or trailing whitespace
        var commandline = input.trim();

        // If the input is empty, do nothing
        if (commandline.isEmpty()) {
            return;
        }

        // Separate the command and arguments
        String command;
        String[] argv;
        if (!commandline.contains(" ")) {
            // If no arguments, set argv as an empty array
            argv = new String[0];
            command = commandline;
        } else {
            // Split the commandline at the space to separate command and arguments
            argv = commandline.split(" ");
            command = argv[0];
            // Remove the command from the arguments array
            argv = ArrayUtils.remove(argv, 0);
        }
        // Log the command being executed
        UltracraftServer.LOGGER.info(this.getName() + " ran command: " + commandline);

        // Retrieve the base command from the registry
        Command baseCommand = CommandRegistry.get(command);
        if (baseCommand == null) {
            // If the command is not found, send an error message
            Chat.sendError(this, "Unknown command&: " + command);
            return;
        }

        if (!baseCommand.onCommand(this, new CommandContext(command), command, argv)) {
            // If the command fails, send an error message
            Chat.sendError(this, "Command failed&: " + command);
        }
    }
}
