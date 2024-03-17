package com.ultreon.craft.api.commands;

import java.util.List;

/**
 * Interface for command tab completer
 */
public interface CommandTabCompleter {
    /**
     * Tab complete method for command tab completer
     *
     * @param sender     the command sender
     * @param commandCtx the command context
     * @param ctx        the command reader
     * @param args       the command arguments
     * @return a list of tab completion options
     * @throws CommandParseException if an error occurs during tab completion
     */
    List<String> tabComplete(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException;
}