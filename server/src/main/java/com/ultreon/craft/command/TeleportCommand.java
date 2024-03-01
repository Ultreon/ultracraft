package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

public class TeleportCommand extends Command {
    public TeleportCommand() {
        this.requirePermission("ultracraft.commands.teleport");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("teleport", "tp");
    }

    @SubCommand("coords <position>")
    public @Nullable CommandOutput executeCoords(CommandSender sender, CommandContext commandContext, String alias, Vec3d position) {
        if (!(sender instanceof ServerPlayer player)) return this.needPlayer();

        player.teleportTo(position.x, position.y, position.z);

        return this.successMessage("Teleported to " + position);
    }

    @SubCommand("coords <position> <world>")
    public @Nullable CommandOutput executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias, Vec3d position, ServerWorld world) {
        if (!(sender instanceof ServerPlayer player)) return this.needPlayer();

        player.getWorld().despawn(player);
        player.teleportTo(position.x, position.y, position.z);
        world.spawn(player);

        return this.successMessage("Teleported to " + position + " at " + world.getDimension().getName());
    }

    @SubCommand("relative <position>")
    public @Nullable CommandOutput executeRelative(CommandSender sender, CommandContext commandContext, String alias, Vec3d offset) {
        if (!(sender instanceof ServerPlayer player)) return this.needPlayer();

        Vec3d add = player.getPosition().add(offset);
        player.teleportTo(add.x, add.y, add.z);

        return this.successMessage("Teleported to " + player.getPosition());
    }
}
