package com.ultreon.craft.api.commands.selector;

import com.google.common.collect.Lists;
import com.ultreon.craft.api.commands.CommandContext;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.UltracraftServer;

import java.util.ArrayList;
import java.util.UUID;

public class CommandSenderBaseSelector extends BaseSelector<CommandSender> {
    private final CommandSender sender;

    public CommandSenderBaseSelector(CommandSender sender, Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.result = this.calculateData();
    }

    public CommandSenderBaseSelector(CommandSender sender, String text) {
        super(text);
        this.sender = sender;
        this.result = this.calculateData();
    }

    @Override
    public BaseSelector.Result<CommandSender> calculateData() {
        Player player = null;
        if (this.sender instanceof Player) {
            player = (Player) this.sender;
        }
        if (this.error != null) {
            return new Result<>(null, this.error);
        }
        Entity target;
        switch (this.key) {
            case TAG:
                switch (this.stringValue) {
                    case "target":
                        if (player == null) {
                            return new Result<>(null, new NeedPlayerError());
                        }
                        target = player.rayCast(player.getWorld().getEntities());
                        try {
                            return new Result<>(target, null);
                        } catch (ClassCastException e) {
                            return new Result<>(null, new TargetNotFoundError("entity"));
                        }

                    case "me":
                        return new Result<>(this.sender, null);

                    case "nearest":
                        if (player == null) {
                            return new Result<>(null, new NeedPlayerError());
                        }
                        target = player.nearestEntity();
                        return target == null ? new Result<>(null, new NotFoundInWorldError("entity"))
                                                : new Result<>(target, null);

                    case "selected":
                        return new Result<>(null, new NoSelectedError("entity"));

                    case "console":
                        return new Result<>(UltracraftServer.get().getConsoleSender(), null);
                }
                return new Result<>(null, new OverloadError());

            case UUID:
                if ("00000000-0000-0000-0000-000000000000".equals(this.stringValue)) {
                    return new Result<>(UltracraftServer.get().getConsoleSender(), null);
                }
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(this.stringValue);
                    target = UltracraftServer.get().getEntity(uuid);
                    if (target == null) {
                        return new Result<>(null, new NotFoundError("command sender"));
                    }
                } catch (IllegalArgumentException e) {
                    if (uuid == null) {
                        return new Result<>(null, new InvalidUUIDError());
                    }
                    e.printStackTrace();
                    return new Result<>(null, new ImpossibleError("Got error that couldn't be caught."));
                }
                break;

            case NAME:
                if ("$".equals(this.stringValue)) {
                    return new Result<>(UltracraftServer.get().getConsoleSender(), null);
                }
                try {
                    target = UltracraftServer.get().getPlayer(this.stringValue);
                    if (target == null) {
                        return new Result<>(null, new NotFoundError("command sender"));
                    }
                } catch (IllegalArgumentException e) {
                    return new Result<>(null, new InvalidUUIDError());
                }
                break;

            default:
                return new Result<>(null, new OverloadError());
        }
        return new Result<>(target, null);
    }

    public static ArrayList<String> tabComplete(CommandSender sender, CommandContext commandCtx, String arg){
        return tabComplete(true, sender, commandCtx, arg);
    }

    public static ArrayList<String> tabComplete(boolean canBeSender, CommandSender sender, CommandContext commandCtx, String arg) {
        ArrayList<String> output = new ArrayList<>();
        if (sender instanceof Player) {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "target", "nearest", "selected");
            if (canBeSender) {
                TabCompleting.selectors(output, SelectorKey.TAG, arg, "me");
            }
        } else {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "selected");
        }
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.entityUuids(Lists.newArrayList("00000000-0000-0000-0000-000000000000"), ""));
        TabCompleting.selectors(output, SelectorKey.NAME, arg, TabCompleting.onlinePlayers(Lists.newArrayList("$"), ""));
        return output;
    }
}