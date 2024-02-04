package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.CommandSpec;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.api.commands.CommandData;
import com.ultreon.craft.server.chat.Chat;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;

public abstract class CommandError implements CommandOutput {
    protected final String message;
    private final MessageCode messageCode;
    private int index;
    private boolean isGeneric = false;
    private boolean onlyOverloads = false;

    public CommandError(String msg) {
        this(MessageCode.GENERIC, msg);
    }

    public CommandError(String msg, int index) {
        this(MessageCode.GENERIC, msg, index);
    }

    public CommandError(MessageCode messageCode, String msg) {
        this.message = msg;
        this.index = -1;
        this.messageCode = messageCode;
    }

    public CommandError(MessageCode messageCode, String msg, int index) {
        this.message = msg;
        this.index = index;
        this.messageCode = messageCode;
    }

    public CommandError setIndex(int index) {
        this.index = index;
        return this;
    }

    public CommandError addIndex(int i) {
        this.index += i;
        return this;
    }

    @Override
    public void send(CommandSender sender) {
        if (this.index >= 0) {
            TextObject argErr = Chat.formatError(sender, "<!>" + this.message + " - At argument " + this.index, this.getName());
            sender.sendMessage(argErr);
        } else {
            MutableText msgErr = Chat.formatError(sender, "<!>" + this.message, this.getName());
            sender.sendMessage(msgErr);
        }
        MutableText msgErr = Chat.formatError(sender, "<!>" + this.message, this.getName());
        MutableText msgCode = Chat.formatError(sender, "  <gray-12>" + "Error code: " + this.messageCode.getCode() + " (" + this.messageCode + ")", this.getName());
        sender.sendMessage(msgErr);
        sender.sendMessage(msgCode);
    }

    public abstract String getName();

    public void send(CommandSender sender, CommandData cmdData) {
        if (this.isGeneric) {
            this.send(sender);
            return;
        }
        if (this.onlyOverloads) {
            for (CommandSpec key : cmdData.getOverloads().keySet()) {
                MutableText text = Chat.formatError(sender, "  <gray-12>" + key.toString().replace("<", "&<"), this.getName());
                sender.sendMessage(text);
            }
        } else {
            cmdData.sendUsage(this.getName(), sender);
        }
        if (this.index >= 0) {
            MutableText argErr = Chat.formatError(sender, "<!>" + this.message + " - At argument " + this.index, this.getName());
            sender.sendMessage(argErr);
        } else {
            MutableText msgErr = Chat.formatError(sender, "<!>" + this.message, this.getName());
            sender.sendMessage(msgErr);
        }
        MutableText msgCode = Chat.formatError(sender, "  <gray-12>" + "Error code: " + this.messageCode.getCode() + " (" + this.messageCode + ")", this.getName());
        sender.sendMessage(msgCode);
    }

    protected void setGeneric() {
        this.isGeneric = true;
    }

    public void setOnlyOverloads(boolean onlyOverloads) {
        this.onlyOverloads = onlyOverloads;
    }

    public String getMessage() {
        return this.message;
    }

    public MessageCode getMessageCode() {
        return this.messageCode;
    }

    public int getIndex() {
        return this.index;
    }
}