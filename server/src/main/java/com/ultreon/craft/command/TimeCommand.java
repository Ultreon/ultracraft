package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.network.packets.s2c.S2CTimePacket;
import com.ultreon.craft.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class TimeCommand extends Command {
    public TimeCommand() {
        this.requirePermission("ultracraft.commands.gamemode");
        this.setCategory(CommandCategory.CHEATS);
        this.data().aliases("time", "t");
    }

    @DefineCommand("add <int>")
    public @Nullable CommandResult executeAdd(CommandSender sender, CommandContext commandContext, String alias, int time) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(new S2CTimePacket(S2CTimePacket.Operation.ADD, time));
        }

        return this.successMessage("Added time: " + time);
    }

    @DefineCommand("set <int>")
    public @Nullable CommandResult executeSet(CommandSender sender, CommandContext commandContext, String alias, int time) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(new S2CTimePacket(S2CTimePacket.Operation.SET, time));
        }

        return this.successMessage("Set time: " + time);
    }

    @DefineCommand("sub <int>")
    public @Nullable CommandResult executeSubtract(CommandSender sender, CommandContext commandContext, String alias, int time) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(new S2CTimePacket(S2CTimePacket.Operation.SUB, time));
        }

        return this.successMessage("Subtracted time: " + time);
    }
}
