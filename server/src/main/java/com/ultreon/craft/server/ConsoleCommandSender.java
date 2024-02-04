package com.ultreon.craft.server;

import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.text.Formatter;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConsoleCommandSender implements CommandSender {
    @Override
    public @NotNull Location getLocation() {
        return new Location(World.OVERWORLD, 0, 0, 0, 0, 0);
    }

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public @Nullable String getPublicName() {
        return "<red>Console";
    }

    @Override
    public TextObject getDisplayName() {
        return Formatter.format(this.getPublicName());
    }

    @Override
    public UUID getUuid() {
        return UUID.nameUUIDFromBytes("Hello Console".getBytes());
    }

    @Override
    public void sendMessage(@NotNull String message) {

    }

    @Override
    public void sendMessage(@NotNull TextObject component) {

    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return false;
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}
