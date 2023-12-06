package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import org.jetbrains.annotations.Nullable;

public class HelloWorldCommand extends Command {
    public HelloWorldCommand() {
        this.requirePermission("ultracraft.commands.helloworld");
        this.setCategory(CommandCategory.FUN);
        this.data().aliases("helloworld", "hello");
    }

    @SubCommand
    public @Nullable CommandOutput execute(CommandSender sender, CommandContext commandContext, String alias) {
        return this.infoMessage("Hello!");
    }

    @SubCommand("<string>")
    public @Nullable CommandOutput execute(CommandSender sender, CommandContext commandContext, String alias, String playerName) {
        return this.infoMessage("Hello, " + playerName);
    }
}
