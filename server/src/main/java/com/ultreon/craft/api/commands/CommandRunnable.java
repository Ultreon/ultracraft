package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.output.CommandResult;

@FunctionalInterface
public interface CommandRunnable {
    CommandResult invoke(Object... objects);
}