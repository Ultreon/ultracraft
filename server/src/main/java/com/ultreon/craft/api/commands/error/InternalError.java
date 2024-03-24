package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class InternalError extends CommandError {
    private final String name;

    public InternalError(String msg) {
        super(MessageCode.SERVER_ERROR, "An unknown internal error occurred: " + msg);
        this.setGeneric();
        this.name = "Internal";
    }

    @Override
    public String getName() {
        return this.name;
    }
}