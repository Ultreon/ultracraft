package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

public class TeleportCommand extends Command {
    public TeleportCommand() {
        this.requirePermission("ultracraft.commands.helloworld");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("teleport", "tp");
    }

    @SubCommand("coords <position>")
    public @Nullable CommandOutput executeCoords(CommandSender sender, CommandContext commandContext, String alias, Vec3d position) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        player.setPosition(position);

        return this.successMessage("Teleported to " + position);
    }

    @SubCommand("coords <position> <world>")
    public @Nullable CommandOutput executeCoordsInWorld(CommandSender sender, CommandContext commandContext, String alias, Vec3d position, ServerWorld world) {
        if (!(sender instanceof Player player)) return this.needPlayer();

        player.getWorld().despawn(player);
        player.setPosition(position);
        world.spawn(player);

        return this.successMessage("Teleported to " + position + " at " + world.getDimension().getName());
    }
}
