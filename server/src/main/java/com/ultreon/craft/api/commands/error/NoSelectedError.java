package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class NoSelectedError extends CommandError {

    private final String name;

    public NoSelectedError(String what) {
        super(MessageCode.NO_SELECTION, "There's no selected " + what + "!");
        this.name = "NotFound";
    }

    public NoSelectedError(String what, int index) {
        super(MessageCode.NO_SELECTION, "There's no selected " + what + "!", index);
        this.name = "NotFound";
    }

    @Override
    public String getName() {
        return this.name;
    }
}