package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.api.ubo.UboFormatter;
import com.ultreon.craft.entity.LivingEntity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.text.TextObject;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

public class PlayerCommand extends Command {
    public PlayerCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("player", "person");
    }

    @SubCommand("dump-data <player>")
    public @Nullable CommandOutput executeDumpData(CommandSender sender, CommandContext commandContext, String alias, Player player) {
        MapType save = player.save(new MapType());

        TextObject formatted = UboFormatter.format(save);
        sender.sendMessage(TextObject.translation("ultracraft.commands.player.dumpData.success").append(formatted));

        return null;
    }
}
