package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        this.requirePermission("ultracraft.commands.setblock");
        this.setCategory(CommandCategory.EDIT);
        this.data().aliases("setblock", "sb");
    }

    @SubCommand("<location> <block>")
    public @Nullable CommandOutput execute(CommandSender sender, CommandContext commandContext, String alias, Location location, Block block) {
        World world = location.getSeverWorld();

        if (sender instanceof Entity entity && world == null) world = entity.getWorld();
        if (world == null) return this.needEntity();

        world.set(location.getBlockPos(), block);

        return this.successMessage("Set block at %s to %s".formatted(location, block));
    }
}
