package com.ultreon.craft.api.commands.selector;

import com.ultreon.craft.api.commands.CommandContext;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.Selections;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerBaseSelector extends BaseSelector<Player> {

    private CommandSender sender;

    public PlayerBaseSelector(CommandSender sender, BaseSelector.Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.result = this.calculateData();
    }

    public PlayerBaseSelector(CommandSender sender, String text) {
        super(text);
        this.sender = sender;
        this.result = this.calculateData();
    }

    @Override
    public Result<Player> calculateData() {
        Player player = null;
        if (this.sender instanceof Player) {
            player = (Player) this.sender;
        }
        if (this.error != null) {
            return new Result<>(null, this.error);
        }
        Object target0;
        Player target = null;
        if (this.key == SelectorKey.TAG) {
            switch (this.stringValue) {
                case "target":
                    if (player == null) {
                        return new Result<>(null, new NeedPlayerError());
                    }
                    target0 = player.rayCast(player.getWorld().getEntities());
                    if (target0 == null) {
                        return new Result<>(null, new TargetPlayerNotFoundError());
                    }
                    return (target0 instanceof Player)
                            ? new Result<>((Player) target0, null)
                            : new Result<>(null, new TargetPlayerNotFoundError());

                case "me":
                    return (this.sender instanceof Player)
                            ? new Result<>((Player) this.sender, null)
                            : new Result<>(null, new NeedPlayerError());

                case "nearest":
                    if (player == null) {
                        return new Result<>(null, new NeedPlayerError());
                    }
                    target0 = player.nearestEntity(Player.class);
                    return (target0 == null)
                            ? new Result<>(null, new NotFoundInWorldError("player"))
                            : new Result<>((Player) target0, null);

                case "selected":
                    target0 = Selections.get(this.sender).getPlayer();
                    if (target0 == null) {
                        return new Result<>(null, new NoSelectedError("player"));
                    }
                    return new Result<>((Player) target0, null);

                default:
                    return new Result<>(null, new OverloadError());
            }
        }
        else if (this.key == SelectorKey.NAME) {
            String name = this.stringValue;
            target = UltracraftServer.get().getPlayer(name);
            if (target instanceof ServerPlayer) {
                return new Result<>(target, null);
            } else {
                return new Result<>(null, new NotFoundError("player " + name));
            }

        } else if (this.key == SelectorKey.UUID) {
            try {
                UUID uuid = UUID.fromString(this.stringValue);
                target = UltracraftServer.get().getPlayer(uuid);
                if (target == null) {
                    return new Result<>(null, new NotFoundError("player with uuid " + uuid));
                }
                return new Result<>(target, null);
            } catch(IllegalArgumentException e) {
                return new Result<>(null, new InvalidUUIDError());
            }
        } else {
            return new Result<>(null, new OverloadError());
        }
    }

    public static ArrayList<String> tabComplete(CommandSender sender, CommandContext commandCtx, String arg) {
        return PlayerBaseSelector.tabComplete(true, sender, commandCtx, arg);
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

        TabCompleting.selectors(output, SelectorKey.NAME, arg, TabCompleting.onlinePlayers(new ArrayList<>(), ""));
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.entityUuids(new ArrayList<>(), "", Player.class));

        return output;
    }
}