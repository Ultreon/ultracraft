package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.Gamemode;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        this.requirePermission("ultracraft.commands.gamemode");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("gamemode", "gm");
    }

    @SubCommand("<gamemode>")
    public @Nullable CommandOutput executeCoords(CommandSender sender, CommandContext commandContext, String alias, Gamemode gamemode) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        player.setGamemode(gamemode);

        return this.successMessage("Gamemode set to %s".formatted(gamemode.name().toLowerCase(Locale.ROOT)));
    }
}
