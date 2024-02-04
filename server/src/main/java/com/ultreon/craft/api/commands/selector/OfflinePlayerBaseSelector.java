package com.ultreon.craft.api.commands.selector;

import com.ultreon.craft.api.commands.CommandContext;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.CachedPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class OfflinePlayerBaseSelector extends BaseSelector<CachedPlayer> {
    public OfflinePlayerBaseSelector(CommandSender sender, Parsed parsed) {
        super(parsed);
        this.result = this.calculateData();
    }

    public OfflinePlayerBaseSelector(CommandSender sender, String text) {
        super(text);
        this.result = this.calculateData();
    }

    @Override
    public Result<CachedPlayer> calculateData() {
        if (this.error != null) {
            return new Result<>(null, this.error);
        }
        CachedPlayer target;
        switch (this.key) {
            case NAME -> {
                CachedPlayer cachedPlayer = UltracraftServer.get().getCachedPlayer(this.stringValue);
                if (cachedPlayer == null) return new Result<>(null, new NotFoundError("player " + this.stringValue));
                if (cachedPlayer.isOnline()) return new Result<>(null, new PlayerIsOnlineError(this.stringValue));
                target = cachedPlayer;
            }
            case UUID -> {
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(this.stringValue);
                    CachedPlayer cachedPlayer = UltracraftServer.get().getCachedPlayer(uuid);
                    if (cachedPlayer == null)
                        return new Result<>(null, new NotFoundError("player " + this.stringValue));
                    if (cachedPlayer.isOnline())
                        return new Result<>(null, new PlayerIsOnlineError(cachedPlayer.getName()));
                    target = UltracraftServer.get().getCachedPlayer(uuid);
                } catch (IllegalArgumentException e) {
                    if (uuid == null) {
                        return new Result<>(null, new InvalidUUIDError());
                    }
                    e.printStackTrace();
                    return new Result<>(null, new ImpossibleError("Got error that couldn't be caught."));
                }
            }
            default -> {
                return new Result<>(null, new OverloadError());
            }
        }
        return new Result<>(target, null);
    }

    public static ArrayList<String> tabComplete(CommandSender sender, CommandContext commandCtx, String arg) {
        return tabComplete(true, sender, commandCtx, arg);
    }

    public static ArrayList<String> tabComplete(boolean canBeSender, CommandSender sender, CommandContext commandCtx, String arg) {
        ArrayList<String> output = new ArrayList<>();
        TabCompleting.selectors(output, SelectorKey.NAME, arg, TabCompleting.offlinePlayers(new ArrayList<>(), ""));
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.offlinePlayerUuids(new ArrayList<>(), ""));
        return output;
    }
}