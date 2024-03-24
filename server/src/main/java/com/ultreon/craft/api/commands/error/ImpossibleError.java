package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class ImpossibleError extends CommandError {

    public ImpossibleError(String msg) {
        super(MessageCode.IMPOSSIBLE, "Good job! You triggered the <b><_>IMPOSSIBLE</> error: <i>" + msg + "</i>");
        this.setGeneric();
    }

    @Override
    public String getName() {
        return "Impossible";
    }
}