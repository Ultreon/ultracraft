package com.ultreon.craft.api.commands.output;

import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.server.chat.Chat;

public class BasicCommandOutput implements CommandOutput {
    private final String message;
    private final MessageType type;

    public BasicCommandOutput(String message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    @Override
    public void send(CommandSender sender) {
        switch (this.type) {
            case SERVER -> Chat.sendServerMessage(sender, this.message);
            case SUCCESS -> Chat.sendSuccess(sender, this.message);
            case INFO -> Chat.sendInfo(sender, this.message);
            case WARNING -> Chat.sendWarning(sender, this.message);
            case DENIED -> Chat.sendDenied(sender, this.message);
            case ERROR -> Chat.sendError(sender, this.message);
            case FATAL -> Chat.sendFatal(sender, this.message);
        }
    }

    public enum MessageType {
        SERVER, SUCCESS, INFO, WARNING, DENIED, EDIT_MODE, ERROR, FATAL
    }
}