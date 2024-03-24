package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class NeedLivingEntityError extends CommandError {

    public NeedLivingEntityError() {
        super(MessageCode.NEED_ENTITY, "You need to be a living entity to use this command!");
    }

    public NeedLivingEntityError(int index) {
        super(MessageCode.NEED_ENTITY, "You need to be a living entity to use this command!", index);
    }

    @Override
    public String getName() {
        return "NotEntity";
    }
}