package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.output.BasicCommandResult;

public enum CommandFlag {

    DANGEROUS(
            MessageCode.DANGEROUS,
            BasicCommandResult.MessageType.WARNING,
            "This is a dangerous command, use it at your own risk."
    ),
    EDIT_MODE(
            MessageCode.EDIT_MODE,
            BasicCommandResult.MessageType.WARNING,
            "This command edits the world, use it at your own risk."
    );

    private final MessageCode messageCode;
    private final BasicCommandResult.MessageType messageType;
    private final String description;

    CommandFlag(MessageCode messageCode, BasicCommandResult.MessageType messageType, String description) {
        this.messageCode = messageCode;
        this.messageType = messageType;
        this.description = description;
    }

    public MessageCode getMessageCode() {
        return this.messageCode;
    }

    public BasicCommandResult.MessageType getMessageType() {
        return this.messageType;
    }

    public String getDescription() {
        return this.description;
    }
}