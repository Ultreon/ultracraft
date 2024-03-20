package com.ultreon.craft.api.commands.output;

import com.ultreon.craft.api.commands.CommandSender;

public interface CommandResult {
    void send(CommandSender sender);
}