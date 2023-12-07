package com.ultreon.craft.client.player;

import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.network.packets.AbilitiesPacket;
import com.ultreon.craft.util.Gamemode;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;

public class RemotePlayer extends ClientPlayer {
    private String name = "<Player>";

    public RemotePlayer(World world) {
        super(EntityTypes.PLAYER, world);
    }

    public @NotNull String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void sendAbilities() {

    }

    @Override
    protected void onAbilities(@NotNull AbilitiesPacket packet) {

    }

    @Override
    public void setGamemode(@NotNull Gamemode gamemode) {

    }
}
