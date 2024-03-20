package com.ultreon.craft.api.commands;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.ServerChunk;
import com.ultreon.craft.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class Selections {
    private Player player = null;
    private Entity entity = null;
    private ServerWorld world = null;
    private ServerChunk chunk = null;
    protected PositionCommand.PositionSelection positions = new PositionCommand.PositionSelection();
    private static Map<CommandSender, Selections> selections = new HashMap<>();

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.entity = player;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.player = (entity instanceof Player) ? (Player) entity : null;
        this.entity = entity;
    }

    public static Selections get(CommandSender sender) {
        if (selections.containsKey(sender)) {
            return selections.get(sender);
        } else {
            Selections select = new Selections();
            selections.put(sender, select);
            return select;
        }
    }

    public ServerWorld getWorld() {
        return world;
    }

    public void setWorld(ServerWorld world) {
        this.world = world;
    }

    public ServerChunk getChunk() {
        return chunk;
    }

    public void setChunk(ServerChunk chunk) {
        this.chunk = chunk;
    }
}