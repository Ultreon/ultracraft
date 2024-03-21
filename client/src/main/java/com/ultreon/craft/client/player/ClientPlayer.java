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
    public float bop;
    public boolean inverseBop;
    public float bopZ;
    public boolean inverseBopZ;
    private float xRot0;
    private float yRot0;
    private float xHeadRot0;
    private float oXRot;
    private float oYRot;
    private float oXHeadRot;

    protected ClientPlayer(EntityType<? extends Player> entityType, World world) {
        super(entityType, world, UltracraftClient.get().getUser().name());
    }

    @Override
    public void tick() {
        super.tick();

        this.oXRot = this.xRot;
        this.oYRot = this.yRot;
        this.oXHeadRot = this.xHeadRot;
        this.xRot0 = this.xRot;
        this.yRot0 = this.yRot;
        this.xHeadRot0 = this.xHeadRot;
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

    @Override
    public void dropItem() {
        super.dropItem();
    }

    public Vec3d getLookVector(float partialTick) {
        // Calculate the direction vector
        Vec3d direction = new Vec3d();

        float yRot = this.getYRot(partialTick);
        float xHeadRot = this.getHeadXRot(partialTick);
        direction.x = (float) (Math.cos(Math.toRadians(yRot)) * Math.sin(Math.toRadians(xHeadRot)));
        direction.z = (float) (Math.cos(Math.toRadians(yRot)) * Math.cos(Math.toRadians(xHeadRot)));
        direction.y = (float) (Math.sin(Math.toRadians(yRot)));

        // Normalize the direction vector
        direction.nor();
        return direction;
    }
}
