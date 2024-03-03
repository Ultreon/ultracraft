package com.ultreon.craft.api.commands.selector;

import com.ultreon.craft.api.commands.Command;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.World;

import java.util.ArrayList;

public class WorldBaseSelector extends BaseSelector<World> {
    private final CommandSender sender;

    public WorldBaseSelector(CommandSender sender, Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.result = this.calculateData();
    }

    public WorldBaseSelector(CommandSender sender, String text) {
        super(text);
        this.sender = sender;
        this.result = this.calculateData();
    }

    @Override
    public Result<World> calculateData() {
        Player player = null;
        if (this.sender instanceof Player) {
            player = (Player) this.sender;
        }
        if (this.getError() != null) {
            return new Result<>(null, this.getError());
        }
        World target = null;
        if (this.getKey() == SelectorKey.TAG) {
            if ("here".equals(this.getStringValue())) {
                if (player == null) {
                    return new Result<>(null, new NeedPlayerError());
                } else {
                    return new Result<>(player.getWorld(), null);
                }
            } else {
                return new Result<>(null, new OverloadError());
            }
        } else if (this.getKey() == SelectorKey.NAME) {
            String name = this.getStringValue();
            Identifier identifier = Identifier.tryParse(name);
            if (identifier == null) {
                return new Result<>(null, new InvalidKeyError(name));
            }
            target = UltracraftServer.get().getWorld(identifier);
            if (target == null) {
                return new Result<>(null, new NotFoundError("world " + name));
            }
        }
        if (target == null) {
            return new Result<>(null, new ImpossibleError("Got error that couldn't be caught."));
        } else {
            return new Result<>(target, null);
        }
    }

    public static ArrayList<String> tabComplete(CommandSender sender, Command command, String arg) {
        ArrayList<String> output = new ArrayList<>();
        if (sender instanceof Player) {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "here");
        }
        TabCompleting.selectors(output, SelectorKey.NAME, arg, TabCompleting.worlds(new ArrayList<>(), ""));
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.worldIds(new ArrayList<>(), ""));
        return output;
    }
}