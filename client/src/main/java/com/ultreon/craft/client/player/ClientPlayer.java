package com.ultreon.craft.client.player;

import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.MathHelper;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.NotNull;

public abstract class ClientPlayer extends Player {
    public float bodyXRot;
    public float walkAnim;
    public boolean inverseAnim;
    public float bop;
    public boolean inverseBop;
    public float bopZ;
    public boolean inverseBopZ;
    public boolean walking;
    private float xRot0;
    private float yRot0;
    private float xHeadRot0;

    protected ClientPlayer(EntityType<? extends Player> entityType, World world) {
        super(entityType, world, UltracraftClient.get().getUser().name());
    }

    public boolean isWalking() {
        return this.walking;
    }

    public float getHeadXRot(float partialTick) {
        return MathHelper.lerp(partialTick, this.xHeadRot0, this.xHeadRot);
    }

    public float getXRot(float partialTick) {
        return MathHelper.lerp(partialTick, this.xRot0, this.xRot);
    }

    public float getYRot(float partialTick) {
        return MathHelper.lerp(partialTick, this.yRot0, this.yRot);
    }

    public double getX(float partialTick) {
        return MathHelper.lerp(partialTick, this.ox, this.x);
    }

    public double getY(float partialTick) {
        return MathHelper.lerp(partialTick, this.oy, this.y);
    }

    public double getZ(float partialTick) {
        return MathHelper.lerp(partialTick, this.oz, this.z);
    }

    public Vec3d getPosition(float partialTick, Vec3d vec) {
        vec.x = MathHelper.lerp(partialTick, this.ox, this.x);
        vec.y = MathHelper.lerp(partialTick, this.oy, this.y);
        vec.z = MathHelper.lerp(partialTick, this.oz, this.z);
        return vec;
    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return false;
    }
}
