package com.ultreon.craft.api.commands;

import java.util.List;

public interface CommandTabCompleter {
    List<String> tabComplete(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException;
}