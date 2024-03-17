package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.entity.Player;
import org.jetbrains.annotations.Nullable;

public class FlyCommand extends Command {
    public FlyCommand() {
        this.requirePermission("ultracraft.commands.flight");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("allowFlight", "fly");
    }

    @DefineCommand
    @Perm("ultracraft.commands.flight.self")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        player.setAllowFlight(!player.isAllowFlight());

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }

    @DefineCommand("<player>")
    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        if (player != sender && sender.hasPermission("ultracraft.commands.flight.others")) return this.errorMessage("You cannot use this command on others");

        player.setAllowFlight(!player.isAllowFlight());

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }

    @DefineCommand("<player> <boolean>")
    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext commandContext, String alias, Player player, boolean enable) {
        if (player != sender && sender.hasPermission("ultracraft.commands.flight.others")) return this.errorMessage("You cannot use this command on others");

        if (player.isAllowFlight() == enable) return this.errorMessage("Flight is already set to " + enable);

        player.setAllowFlight(enable);

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }
}
