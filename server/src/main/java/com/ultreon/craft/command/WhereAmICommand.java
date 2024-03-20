package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class WhereAmICommand extends Command {
    public WhereAmICommand() {
        this.requirePermission("ultracraft.commands.helloworld");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("whereAmI", "pos");
    }

    @DefineCommand
    public @Nullable CommandResult executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof Entity entity)) return this.needEntity();

        return this.infoMessage("You are at %s".formatted(entity.getBlockPos()));
    }
}
