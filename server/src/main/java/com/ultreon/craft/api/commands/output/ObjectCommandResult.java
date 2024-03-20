package com.ultreon.craft.api.commands.output;

import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.server.chat.Chat;

public class ObjectCommandResult implements CommandResult {
    private final Object object;
    private final TYPE type;

    public ObjectCommandResult(Object object, TYPE type) {
        this.object = object;
        this.type = type;
    }

    @Override
    public void send(CommandSender sender) {
        switch (this.type) {
            case VOID:
                Chat.sendVoidObject(sender);
                break;

            case OBJECT:
                Chat.sendObject(sender, this.object);
                break;
        }
    }

    public enum TYPE {
        VOID, OBJECT
    }
}