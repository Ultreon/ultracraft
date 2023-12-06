package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class PermissionsBrokenError extends CommandError {
    private String name;

    public PermissionsBrokenError() {
        super(MessageCode.NO_PERMISSION, "Access denied to broken command.");
        this.setGeneric();
        this.setName("Denied");
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}