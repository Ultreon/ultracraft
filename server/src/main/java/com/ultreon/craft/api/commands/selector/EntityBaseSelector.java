package com.ultreon.craft.api.commands.selector;

import com.ultreon.craft.api.commands.CommandContext;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.Selections;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.UltracraftServer;

import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("unused")
public class EntityBaseSelector<T extends Entity> extends BaseSelector<T> {
    private final CommandSender sender;
    private final Class<T> clazz;

    public EntityBaseSelector(CommandSender sender, Class<T> clazz, Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.clazz = clazz;
        this.result = this.calculateData();
    }

    public EntityBaseSelector(CommandSender sender, Class<T> clazz, String text) {
        super(text);
        this.sender = sender;
        this.clazz = clazz;
        this.result = this.calculateData();
    }

    @SuppressWarnings({"unchecked", "RedundantVariableInitialization"})
    @Override
    public Result<T> calculateData() {
        Player player = null;
        if (this.sender instanceof Player) {
            player = (Player) this.sender;
        }
        if (this.getError() != null) {
            return new Result<>(null, this.getError());
        }
        Object target;
        switch (this.getKey()) {
            case TAG:
                var stringValue = this.getStringValue();
                if (stringValue == null) {
                    return new Result<>(null, new OverloadError());
                }
                switch (stringValue) {
                    case "target":
                        if (player == null) {
                            return new Result<>(null, new NeedPlayerError());
                        }
                        target = player.rayCast(player.getWorld().getEntitiesByClass(this.clazz));
                        try {
                            return new Result<>((T) target, null);
                        } catch (ClassCastException e) {
                            return new Result<>(null, new TargetEntityNotFoundError(this.clazz.getSimpleName()));
                        }
                    case "me":
                        if (this.clazz.isAssignableFrom(this.sender.getClass())) {
                            return new Result<>((T) this.sender, null);
                        } else {
                            return new Result<>(null, new NeedEntityError());
                        }
                    case "nearest":
                        if (player == null) {
                            return new Result<>(null, new NeedPlayerError());
                        }
                        target = player.nearestEntity(this.clazz);
                        return target == null
                            ? new Result<>(null, new NotFoundInWorldError("entity"))
                            : new Result<>((T) target, null);
                    case "selected":
                        var entity = Selections.get(this.sender).getEntity();
                        if (this.clazz.isInstance(entity)) {
                            return new Result<>((T) entity, null);
                        } else {
                            return new Result<>(null, new NoSelectedError("entity"));
                        }
                    default:
                        return new Result<>(null, new OverloadError());
                }
            case UUID:
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(this.getStringValue());
                    target = UltracraftServer.get().getEntity(uuid);
                    return target != null
                        ? new Result<>((T) target, null)
                        : new Result<>(null, new NotFoundError("entity with uuid " + uuid));
                } catch (IllegalArgumentException e) {
                    return uuid == null
                        ? new Result<>(null, new InvalidUUIDError())
                        : new Result<>(null, new ImpossibleError("Got error that couldn't be caught."));
                }
            default:
                return new Result<>(null, new OverloadError());
        }
    }

    public static ArrayList<String> tabComplete(Class<? extends Entity> clazz, CommandSender sender, CommandContext commandCtx, String arg) {
        return tabComplete(clazz, true, sender, commandCtx, arg);
    }

    @SuppressWarnings("UnusedParameters")
    public static ArrayList<String> tabComplete(Class<? extends Entity> clazz, boolean canBeSender, CommandSender sender, CommandContext commandCtx, String arg) {
        var output = new ArrayList<String>();
        if (sender instanceof Player) {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "target", "nearest", "selected");
            if (canBeSender) {
                TabCompleting.selectors(output, SelectorKey.TAG, arg, "me");
            }
        } else {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "selected");
        }
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.entityUuids(new ArrayList<>(), "", clazz));
        return output;
    }
}