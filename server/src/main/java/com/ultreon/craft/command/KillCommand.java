package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.entity.LivingEntity;
import com.ultreon.craft.entity.Player;
import org.jetbrains.annotations.Nullable;

public class KillCommand extends Command {
    public KillCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("kill", "murder");
    }

    @DefineCommand
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof LivingEntity player)) return this.needLivingEntity();

        player.kill();

        return this.successMessage("You successfully killed yourself");
    }

    @DefineCommand("<player>")
    public @Nullable CommandResult executeCoords(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        if (sender != player && !sender.hasPermission("ultracraft.commands.kill.others")) return this.noPermission();

        player.kill();

        if (sender == player) return this.successMessage("You successfully killed yourself");
        return this.successMessage("You successfully killed " + player.getName());
    }
}
