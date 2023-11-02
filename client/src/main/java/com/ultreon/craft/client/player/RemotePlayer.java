package com.ultreon.craft.client.player;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemotePlayer extends Player {
    @LazyInit private UUID uuid;
    private String name = "<Player>";

    public RemotePlayer(World world) {
        super(EntityTypes.PLAYER, world);
    }

    @Override
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
