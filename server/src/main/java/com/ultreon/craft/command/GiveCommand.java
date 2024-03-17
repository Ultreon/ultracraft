package com.ultreon.craft.command;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GiveCommand extends Command {
    public GiveCommand() {
        this.requirePermission("ultracraft.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("give", "g");
    }

    @DefineCommand("<player> <item>")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Item item) {
        if (sender instanceof Player player) {
            player.inventory.addItem(item.defaultStack());
            return successMessage("Gave " + player.getName() + " " + item.getTranslation().getText());
        }

        return needPlayer();
    }

    @DefineCommand("<player> <item> <int:count>")
    public @Nullable CommandResult execute(CommandSender sender, CommandContext commandContext, String alias, Item item, int count) {
        if (sender instanceof Player player) {
            player.inventory.addItem(new ItemStack(item, count));
            return successMessage("Gave " + player.getName() + " " + count + "x " + item.getTranslation().getText());
        }

        return needPlayer();
    }
}
