package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class KillCommand extends Command {
    public KillCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("kill", "murder");
    }

    @SubCommand
    public @Nullable CommandOutput executeCoords(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof LivingEntity player)) return this.needLivingEntity();

        player.kill();

        return this.successMessage("You successfully killed yourself");
    }
}
