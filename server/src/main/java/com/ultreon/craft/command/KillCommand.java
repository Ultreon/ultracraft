package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class KillCommand extends Command {
    public KillCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.UTILITY);
        this.data().aliases("kill");
    }

    @SubCommand
    public @Nullable CommandOutput executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof LivingEntity entity)) return this.needLivingEntity();

        entity.kill();

        return this.successMessage("Successfully killed " + entity.getName());
    }
}
