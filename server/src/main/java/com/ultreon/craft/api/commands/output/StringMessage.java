package com.ultreon.craft.api.commands.output;

import com.ultreon.craft.api.commands.CommandSender;

public record StringMessage(String text) implements CommandOutput {
    @Override
    public void send(CommandSender sender) {
        sender.sendMessage(this.text);
    }
}