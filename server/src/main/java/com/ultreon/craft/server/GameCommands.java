package com.ultreon.craft.server;

import com.ultreon.craft.api.commands.Command;
import com.ultreon.craft.command.*;
import com.ultreon.craft.registry.CommandRegistry;

public class GameCommands {
    public static void register() {
        CommandRegistry.register(new TeleportCommand());
        CommandRegistry.register(new FlyCommand());
        CommandRegistry.register(new InvincibleCommand());
        CommandRegistry.register(new WhereAmICommand());
        CommandRegistry.register(new GamemodeCommand());

        Command.runCommandLoaders();
    }
}
