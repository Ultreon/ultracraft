package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.api.ubo.UboFormatter;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

public class GiveCommand extends Command {
    public GiveCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("give", "g");
    }

    @SubCommand("<item>")
    public @Nullable CommandOutput execute(CommandSender sender, CommandContext commandContext, String alias, Item item) {
        if (sender instanceof Player player) {
            player.inventory.addItem(item.defaultStack());
            return successMessage("Gave " + player.getName() + " " + item.getTranslation().getText());
        }

        return needPlayer();
    }

    @SubCommand("<item> <int:count>")
    public @Nullable CommandOutput execute(CommandSender sender, CommandContext commandContext, String alias, Item item, int count) {
        if (sender instanceof Player player) {
            player.inventory.addItem(new ItemStack(item, count));
            return successMessage("Gave " + player.getName() + " " + count + "x " + item.getTranslation().getText());
        }

        return needPlayer();
    }
}
