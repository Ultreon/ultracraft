package com.ultreon.craft.api.commands;

import org.jetbrains.annotations.Nullable;

public class CommandExecuteException extends Exception {
    public CommandExecuteException(@Nullable String message) {
        super(message);
    }
}