package com.ultreon.craft.api.commands;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.ServerChunk;
import com.ultreon.craft.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class Selections {
    private Player _player = null;
    private Entity _entity = null;
    private ServerWorld world = null;
    private ServerChunk chunk = null;
    protected PositionCommand.PositionSelection positions = new PositionCommand.PositionSelection();
    private static Map<CommandSender, Selections> selections = new HashMap<>();

    public Player getPlayer() {
        return this._player;
    }

    public void setPlayer(Player player) {
        this._player = player;
        this._entity = player;
    }

    public Entity getEntity() {
        return this._entity;
    }

    public void setEntity(Entity entity) {
        this._player = (entity instanceof Player) ? (Player) entity : null;
        this._entity = entity;
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
}