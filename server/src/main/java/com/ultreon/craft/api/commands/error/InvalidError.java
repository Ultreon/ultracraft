package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class InvalidError extends CommandError {

    public InvalidError(String WHAT) {
        super(MessageCode.INVALID_VALUE, "The " + WHAT + " is invalid!");
    }

    public InvalidError(String WHAT, int index) {
        super(MessageCode.INVALID_VALUE, "The " + WHAT + " is invalid!", index);
    }

    @Override
    public String getName() {
        return "Invalid";
    }
}