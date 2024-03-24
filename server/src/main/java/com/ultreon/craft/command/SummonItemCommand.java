package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.api.ubo.UboFormatter;
import com.ultreon.craft.entity.DroppedItem;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SummonItemCommand extends Command {
    public SummonItemCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("summonItem", "si");
    }

    @SubCommand("<item>")
    public @Nullable CommandOutput execute(CommandSender sender, CommandContext commandContext, String alias, Item item) {
        if (sender instanceof Player player) {
            DroppedItem droppedItem = new DroppedItem(player.getWorld(), item.defaultStack(), player.getPosition(), new Vec3d());
            player.getWorld().spawn(droppedItem);
            return successMessage("Spawned " + droppedItem.getName() + " item");
        }

        return needPlayer();
    }
}
