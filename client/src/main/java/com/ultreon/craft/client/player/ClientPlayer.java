package com.ultreon.craft.client.player;

import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.World;

public abstract class ClientPlayer extends Player {
    public ClientPlayer(EntityType<? extends Player> entityType, World world) {
        super(entityType, world);
    }
}
