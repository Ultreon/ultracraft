package com.ultreon.craft.registry;

import com.ultreon.craft.api.commands.Command;
import com.ultreon.craft.api.commands.CommandContext;

import java.util.*;
import java.util.stream.Stream;

public class CommandRegistry {
    private static final Map<String, Command> COMMAND_MAP = new HashMap<>();

    public static void register(Command command) {
        String[] aliases = command.data().getAliases();
        for (String alias : aliases) {
            CommandRegistry.COMMAND_MAP.put(alias, command);
            command.data().onRegister(new CommandContext(alias));
        }
    }

    public static Stream<Command> getCommands() {
        return CommandRegistry.COMMAND_MAP.values().stream();
    }

    public static Stream<String> getCommandNames() {
        return CommandRegistry.COMMAND_MAP.keySet().stream();
    }

    public static Command get(String command) {
        return CommandRegistry.COMMAND_MAP.get(command);
    }
}
