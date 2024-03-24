package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class InvalidValueError extends CommandError {
    protected String name = "Invalid";

    public InvalidValueError(String what, String got) {
        super(MessageCode.INVALID_VALUE, "Invalid " + what + ", got: " + got);
    }

    public InvalidValueError(String what, String got, int index) {
        super(MessageCode.INVALID_VALUE, "Invalid " + what + ", got: " + got, index);
    }

    public InvalidValueError(String what, int index) {
        super("The " + what + " is invalid!", index);
    }

    public InvalidValueError(String what) {
        super("The " + what + " is invalid" + what);
    }

    @Override
    public String getName() {
        return this.name;
    }
}