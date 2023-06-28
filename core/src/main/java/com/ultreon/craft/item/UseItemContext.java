package com.ultreon.craft.item;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.World;

import java.util.Objects;

public final class UseItemContext {
    private final World world;
    private final Player player;
    private final HitResult result;

    public UseItemContext(World world, Player player, HitResult result) {
        this.world = world;
        this.player = player;
        this.result = result;
    }

    public World world() {
        return this.world;
    }

    public Player player() {
        return this.player;
    }

    public HitResult result() {
        return this.result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        UseItemContext that = (UseItemContext) obj;
        return Objects.equals(this.world, that.world) &&
                Objects.equals(this.player, that.player) &&
                Objects.equals(this.result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.world, this.player, this.result);
    }

    @Override
    public String toString() {
        return "UseItemContext[" +
                "world=" + this.world + ", " +
                "player=" + this.player + ", " +
                "result=" + this.result + ']';
    }


}
