package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.output.CommandOutput;

@FunctionalInterface
public interface CommandRunnable {
    CommandOutput invoke(Object... objects);
}