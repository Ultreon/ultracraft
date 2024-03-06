package com.ultreon.craft.server;

import com.ultreon.craft.api.commands.Command;
import com.ultreon.craft.command.*;
import com.ultreon.craft.events.LoadingEvent;
import com.ultreon.craft.registry.CommandRegistry;

public final class GameCommands {
    public static void register() {
        CommandRegistry.register(new TeleportCommand());
        CommandRegistry.register(new KillCommand());
        CommandRegistry.register(new FlyCommand());
        CommandRegistry.register(new InvincibleCommand());
        CommandRegistry.register(new WhereAmICommand());
        CommandRegistry.register(new GamemodeCommand());
        CommandRegistry.register(new TimeCommand());
        CommandRegistry.register(new PlayerCommand());
        CommandRegistry.register(new GiveCommand());
        CommandRegistry.register(new SummonCommand());
        CommandRegistry.register(new SummonItemCommand());

        LoadingEvent.REGISTER_COMMANDS.factory().onRegisterCommands();

        Command.runCommandLoaders();
    }
}
