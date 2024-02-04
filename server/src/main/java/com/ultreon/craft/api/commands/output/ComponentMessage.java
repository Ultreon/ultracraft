package com.ultreon.craft.api.commands.output;

import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.text.TextObject;

public class ComponentMessage implements CommandOutput {
    private final TextObject component;

    public ComponentMessage(TextObject component) {
        this.component = component;
    }

    @Override
    public void send(CommandSender sender) {
        sender.sendMessage(this.component);
    }
}