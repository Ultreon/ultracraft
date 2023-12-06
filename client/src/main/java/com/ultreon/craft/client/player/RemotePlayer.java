package com.ultreon.craft.client.player;

import com.google.errorprone.annotations.concurrent.LazyInit;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RemotePlayer extends ClientPlayer {
    private String name = "<Player>";

    public RemotePlayer(World world) {
        super(EntityTypes.PLAYER, world);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
