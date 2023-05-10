package com.ultreon.craft.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.util.AxisAlignedBB;
import com.ultreon.craft.util.EnumFacing;
import com.ultreon.craft.util.EnumFacing.Axis;
import com.ultreon.craft.util.EnumFacing.AxisDirection;
import com.ultreon.craft.util.Utils;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

public class Entity {
    private final EntityType<? extends Entity> type;
    private final World world;
    protected float x;
    protected float y;
    protected float z;
    protected float xRot;
    protected float yRot;
    private int id = -1;
    public boolean onGround;
    private float groundY;
    public float velocityX;
    public float velocityY;
    public float velocityZ;
    public float gravity = 0.08F;
    public boolean noGravity;
    private boolean almostOnGround;
    public float jumpVel = 0.4F;
    public boolean jumping;
    private float oldX;
    private float oldY;
    private float oldZ;

    public Entity(EntityType<? extends Entity> entityType, World world) {
        this.type = entityType;
        this.world = world;
    }

    public EntitySize getSize() {
        return this.type.getSize();
    }

    public void tick() {
        var size = getSize();
        AxisAlignedBB boundingBox = this.getBoundingBox(size);

        var oldX = this.x;
        var oldY = this.y;
        var oldZ = this.z;

        this.velocityY -= this.gravity;

        // Check for collisions in the x dimension
        if (this.velocityX != 0) {
            float nextX = this.x + this.velocityX;
            AxisAlignedBB nextBox = boundingBox.offset(this.velocityX, 0, 0);

            AxisAlignedBB collidedX = this.world.collidesWithAnyBlock(nextBox, EnumFacing.byAxis(Axis.X, velocityX > 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE));
            if (collidedX != null) {
                this.x = velocityX > 0 ? collidedX.minX - 0.5000001F : collidedX.maxX + 0.5000001F;
                this.velocityX = 0;
            } else {
                this.x = nextX;
            }
        }

        // Check for collisions in the y dimension
        if (this.velocityY != 0) {
            float nextY = this.y + this.velocityY;
            AxisAlignedBB nextBox = boundingBox.offset(0, this.velocityY, 0);

            AxisAlignedBB collidedY = this.world.collidesWithAnyBlock(nextBox, EnumFacing.byAxis(Axis.Y, velocityY > 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE));
            if (collidedY != null) {
                this.y = velocityY > 0 ? collidedY.minY - size.height() : collidedY.maxY;
                this.velocityY = 0;
            } else {
                this.y = nextY;
            }
        }

        // Check for collisions in the z dimension
        if (this.velocityZ != 0) {
            float nextZ = this.z + this.velocityZ;
            AxisAlignedBB nextBox = boundingBox.offset(0, 0, this.velocityZ);

            AxisAlignedBB collidedZ = this.world.collidesWithAnyBlock(nextBox, EnumFacing.byAxis(Axis.Z, velocityZ > 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE));
            if (collidedZ != null) {
                this.z = velocityZ > 0 ? collidedZ.minZ - 0.5000001F : collidedZ.maxZ + 0.5000001F;
                this.velocityZ = 0;
            } else {
                this.z = nextZ;
            }
        }

        checkOnGround();

        if (this.jumping && this.onGround) {
            this.jump();
        }

        this.velocityX *= 0.98F;
        this.velocityY *= 0.98F;
        this.velocityZ *= 0.98F;

        this.x += this.velocityX;
        this.y = this.almostOnGround || this.onGround ? Math.max(this.y + this.velocityY, this.groundY) : this.y + this.velocityY;
        this.z += this.velocityZ;

//        // Check for collisions in the y dimension again
//        AxisAlignedBB box = boundingBox.offset(this.x - oldX, this.y - oldY, this.z - oldZ);
//        AxisAlignedBB collidedY = this.world.collidesWithAnyBlock(box);
//        if (collidedY != null) {
//            this.y = oldY;
//            this.velocityY = 0;
//        }
    }

    public AxisAlignedBB getBoundingBox(EntitySize size) {
        float x1 = this.x - size.width() / 2;
        float y1 = this.y;
        float z1 = this.z - size.width() / 2;
        float x2 = this.x + size.width() / 2;
        float y2 = this.y + size.height();
        float z2 = this.z + size.width() / 2;
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    public void jump() {
        this.velocityY = this.jumpVel;
    }

    private void checkOnGround() {
        Block block = this.world.get(this.getBlockPos().below());
        if (block == null) {
            this.almostOnGround = false;
            this.onGround = true;
            this.groundY = y;
        } else if (this.y % 1 == 0 && !block.isAir()) {
            this.almostOnGround = false;
            this.onGround = true;
            this.groundY = (int) y;
        } else if (this.y % 1 > 0 && !block.isAir()) {
            this.almostOnGround = true;
            this.onGround = false;
            this.groundY = (int) y;
        } else {
            this.almostOnGround = false;
            this.onGround = false;
        }
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = Mth.clamp(yRot, -90, 90);
    }

    public Vector3 getPosition() {
        return new Vector3(x, y, z);
    }

    public void setPosition(Vector3 position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos getBlockPos() {
        return new BlockPos((int) this.x, (int) this.y, (int) this.z);
    }

    public Vector2 getRotation() {
        return new Vector2(this.xRot, this.yRot);
    }

    public Vector3 getLookVector() {
        // Calculate the direction vector
        Vector3 direction = new Vector3();
        var yRot = Mth.clamp(this.yRot, -89.9F, 90);
        direction.x = MathUtils.cosDeg(yRot) * MathUtils.sinDeg(this.xRot);
        direction.z = MathUtils.cosDeg(yRot) * MathUtils.cosDeg(this.xRot);
        direction.y = MathUtils.sinDeg(yRot);

        // Normalize the direction vector
        direction.nor();
        return direction;
    }

    public void setRotation(Vector2 position) {
        this.xRot = position.x;
        this.yRot = Mth.clamp(position.y, -90, 90);
    }

    public Vector3 getVelocity() {
        return new Vector3(this.velocityX, this.velocityY, this.velocityZ);
    }

    protected void setVelocity(Vector3 velocity) {
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
    }

    public void onPrepareSpawn(MapType spawnData) {

    }

    public World getWorld() {
        return world;
    }

    public int getId() {
        return id;
    }

    @ApiStatus.Internal
    public void setId(int id) {
        this.id = id;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }
}
