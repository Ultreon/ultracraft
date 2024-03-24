package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class NoPermissionError extends CommandError {

    public NoPermissionError() {
        super(MessageCode.NO_PERMISSION, "You have no permission to do that!");
        this.setGeneric();
    }

    @Override
    public String getName() {
        return "Denied";
    }
}