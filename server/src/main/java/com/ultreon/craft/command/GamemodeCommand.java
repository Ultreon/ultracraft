package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.chat.Chat;
import com.ultreon.craft.util.Gamemode;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        this.requirePermission("ultracraft.commands.gamemode");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("gamemode", "gm");
    }

    @DefineCommand("<gamemode>")
    @Perm("ultracraft.commands.gamemode.self")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Gamemode gamemode) {
        if (!(sender instanceof Player target)) return this.needPlayer();

        target.setGamemode(gamemode);

        return this.successMessage("Gamemode set to %s".formatted(gamemode.name().toLowerCase(Locale.ROOT)));
    }

    @DefineCommand("<player> <gamemode>")
    public @Nullable CommandResult executeOnPlayer(CommandSender sender, CommandContext commandContext, String alias, Player target, Gamemode gamemode) {
        if (sender != target && !sender.hasPermission("ultracraft.commands.gamemode.others")) return this.errorMessage("Cannot set gamemode for other players");

        target.setGamemode(gamemode);

        if (sender == target) return this.successMessage("Gamemode set to %s".formatted(gamemode.name().toLowerCase(Locale.ROOT)));
        Chat.sendInfo(target, "%s set your gamemode to %s".formatted(target.getName(), gamemode.name().toLowerCase(Locale.ROOT)));
        return this.successMessage("Gamemode set to %s for %s".formatted(gamemode.name().toLowerCase(Locale.ROOT), target.getName()));
    }
}
