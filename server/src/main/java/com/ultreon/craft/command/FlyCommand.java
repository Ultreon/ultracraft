package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.entity.Player;
import org.jetbrains.annotations.Nullable;

public class FlyCommand extends Command {
    public FlyCommand() {
        this.requirePermission("ultracraft.commands.flight");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("allowFlight", "fly");
    }

    @SubCommand
    public @Nullable CommandOutput executeCoords(CommandSender sender, CommandContext commandContext, String alias) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        player.setAllowFlight(!player.isAllowFlight());

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }

    @SubCommand("<player>")
    public @Nullable CommandOutput executeCoords(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        player.setAllowFlight(!player.isAllowFlight());

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }

    @SubCommand("<player> <boolean>")
    public @Nullable CommandOutput executeCoords(CommandSender sender, CommandContext commandContext, String alias, Player player, boolean enable) {
        if (player.isAllowFlight() == enable) return this.errorMessage("Flight is already set to " + enable);

        player.setAllowFlight(enable);

        if (player.isAllowFlight()) return this.successMessage("Flight has been enabled!");
        else return this.successMessage("Flight has been disabled!");
    }
}
