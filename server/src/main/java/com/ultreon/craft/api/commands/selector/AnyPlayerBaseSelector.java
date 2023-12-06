package com.ultreon.craft.api.commands.selector;

import com.ultreon.craft.api.commands.CommandContext;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.Selections;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.CacheablePlayer;
import com.ultreon.craft.server.player.ServerPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class AnyPlayerBaseSelector extends BaseSelector<CacheablePlayer> {
    private final CommandSender sender;

    public AnyPlayerBaseSelector(CommandSender sender, Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.result = this.calculateData();
    }

    public AnyPlayerBaseSelector(CommandSender sender, String text) {
        super(text);
        this.sender = sender;
        this.result = this.calculateData();
    }

    @Override
    public Result<CacheablePlayer> calculateData() {
        ServerPlayer player = (this.sender instanceof ServerPlayer) ? (ServerPlayer) this.sender : null;
        if (this.error != null) {
            return new Result(null, this.error);
        }
        Object target0;
        CacheablePlayer target;
        switch (this.key) {
            case TAG -> {
                switch (this.stringValue) {
                    case "target" -> {
                        if (player == null) {
                            return new Result<>(null, new NeedPlayerError());
                        }
                        target0 = player.rayCast(player.getWorld().getEntities());
                        if (target0 == null) {
                            return new Result<>(null, new TargetPlayerNotFoundError());
                        }
                        if (!(target0 instanceof ServerPlayer)) {
                            return new Result<>(null, new TargetPlayerNotFoundError());
                        } else {
                            return new Result<>((ServerPlayer) target0, null);
                        }
                    }
                    case "me" -> {
                        if (this.sender instanceof ServerPlayer serverPlayer) {
                            return new Result<>(serverPlayer, null);
                        } else {
                            return new Result<>(null, new NeedPlayerError());
                        }
                    }
                    case "nearest" -> {
                        if (player == null) {
                            return new Result<>(null, new NeedPlayerError());
                        }
                        target0 = player.nearestEntity(ServerPlayer.class);
                        if (target0 == null) {
                            return new Result<>(null, new NotFoundInWorldError("player"));
                        } else {
                            return new Result<>((ServerPlayer) target0, null);
                        }
                    }
                    case "selected" -> {
                        target0 = Selections.get(this.sender).getPlayer();
                        if (target0 == null) {
                            return new Result<>(null, new NoSelectedError("player"));
                        } else {
                            return new Result<>((ServerPlayer) target0, null);
                        }
                    }
                    default -> {
                        return new Result<>(null, new OverloadError());
                    }
                }
            }
            case NAME -> {
                target = UltracraftServer.get().getCacheablePlayer(this.stringValue);
                return new Result<>(target, null);
            }
            case UUID -> {
                UUID uuid;
                try {
                    uuid = UUID.fromString(this.stringValue);
                    target = UltracraftServer.get().getCacheablePlayer(uuid);
                    return new Result<>(target, null);
                } catch (IllegalArgumentException e) {
                    return new Result<>(null, new InvalidUUIDError());
                }
            }
            default -> {
                return new Result<>(null, new OverloadError());
            }
        }
    }

    public static ArrayList<String> tabComplete(CommandSender sender, CommandContext commandCtx, String arg) {
        return tabComplete(true, sender, commandCtx, arg);
    }

    public static ArrayList<String> tabComplete(
        boolean canBeSender,
        CommandSender sender,
        CommandContext commandCtx,
        String arg
    ) {
        ArrayList<String> output = new ArrayList<>();
        if (sender instanceof ServerPlayer) {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "target", "nearest", "selected");
            if (canBeSender) {
                TabCompleting.selectors(output, SelectorKey.TAG, arg, "me");
            }
        } else {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "selected");
        }
        TabCompleting.selectors(output, SelectorKey.NAME, arg, TabCompleting.players(new ArrayList<>(), ""));
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.entityUuids(new ArrayList<>(), "", ServerPlayer.class));
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.offlinePlayerUuids(new ArrayList<>(), ""));
        return output;
    }
}