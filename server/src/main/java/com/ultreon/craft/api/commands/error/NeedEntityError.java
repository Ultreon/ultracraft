package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class NeedEntityError extends CommandError {
    public NeedEntityError() {
        super(MessageCode.NEED_ENTITY, "You need to be a entity to use this command!");
    }

    public NeedEntityError(int index) {
        super(MessageCode.NEED_ENTITY, "You need to be a entity to use this command!", index);
    }

    @Override
    public String getName() {
        return "NotEntity";
    }
}