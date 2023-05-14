package com.ultreon.craft.entity;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.util.BoundingBoxUtils;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import org.jetbrains.annotations.ApiStatus;

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
    public float velocityX;
    public float velocityY;
    public float velocityZ;
    public float gravity = 0.08F;
    public boolean noGravity;
    public float jumpVel = 0.55F;
    public boolean jumping;
    public boolean isColliding;
    public boolean isCollidingX;
    public boolean isCollidingY;
    public boolean isCollidingZ;

    public boolean noClip;
    protected float fallDistance = 0;

    public Entity(EntityType<? extends Entity> entityType, World world) {
        this.type = entityType;
        this.world = world;
    }

    public EntitySize getSize() {
        return this.type.getSize();
    }

    public void tick() {
        var size = getSize();
        BoundingBox boundingBox = this.getBoundingBox(size);

        if (!noGravity) {
            this.velocityY -= this.gravity;
        }

        move(velocityX, velocityY, velocityZ);

        if (this.jumping && this.onGround) {
            this.jump();
        }

        this.velocityX *= 0.91F;
        this.velocityY *= 0.98F;
        this.velocityZ *= 0.91F;

        if (this.onGround) {
            this.velocityX *= 0.8f;
            this.velocityZ *= 0.8f;
        }
    }

    public void move(float dx, float dy, float dz) {
        float oDx = dx;
        float oDy = dy;
        float oDz = dz;
        var ext = this.getBoundingBox();
        if (dx < 0) ext.min.x += dx;
        else ext.max.x += dx;
        if (dy < 0) ext.min.y += dy;
        else ext.max.y += dy;
        if (dz < 0) ext.min.z += dz;
        else ext.max.z += dz;
        ext.update();
        if (this.noClip) {
            x += dx;
            y += dy;
            z += dz;
        } else {
            var boxes = world.collide(ext);
            var pBox = getBoundingBox();
            isColliding = false;
            isCollidingY = false;
            for (BoundingBox box : boxes) {
                var dy2 = BoundingBoxUtils.clipYCollide(box, pBox, dy);
                isColliding |= dy != dy2;
                isCollidingY |= dy != dy2;
                dy = dy2;
            }
            pBox.min.add(0.0f, dy, 0.0f);
            pBox.max.add(0.0f, dy, 0.0f);
            pBox.update();
            isCollidingX = false;
            for (BoundingBox box : boxes) {
                var dx2 = BoundingBoxUtils.clipXCollide(box, pBox, dx);
                isColliding |= dx != dx2;
                isCollidingX |= dx != dx2;
                dx = dx2;
            }
            pBox.min.add(dx, 0.0f, 0.0f);
            pBox.max.add(dx, 0.0f, 0.0f);
            pBox.update();
            isCollidingZ = false;
            for (BoundingBox box : boxes) {
                var dz2 = BoundingBoxUtils.clipZCollide(box, pBox, dz);
                isColliding |= dz != dz2;
                isCollidingZ |= dz != dz2;
                dz = dz2;
            }
            pBox.min.add(0.0f, 0.0f, dz);
            pBox.max.add(0.0f, 0.0f, dz);
            pBox.update();
            this.onGround = oDy != dy && oDy < 0.0f;
            if (oDx != dx) {
                this.velocityX = 0.0f;
            }
            if (oDy != dy) {
                hitGround();
                this.fallDistance = 0.0F;
                this.velocityY = 0.0f;
            } else if (this.velocityY < 0) {
                this.fallDistance += -this.velocityY;
            }
            if (oDz != dz) {
                this.velocityZ = 0.0f;
            }
            this.x = (pBox.min.x + pBox.max.x) / 2.0f;
            this.y = pBox.min.y;
            this.z = (pBox.min.z + pBox.max.z) / 2.0f;
        }
    }

    protected void hitGround() {

    }

    private BoundingBox getBoundingBox() {
        return getBoundingBox(getSize());
    }

    public BoundingBox getBoundingBox(EntitySize size) {
        float x1 = this.x - size.width() / 2;
        float y1 = this.y;
        float z1 = this.z - size.width() / 2;
        float x2 = this.x + size.width() / 2;
        float y2 = this.y + size.height();
        float z2 = this.z + size.width() / 2;
        return new BoundingBox(new Vector3(x1, y1, z1), new Vector3(x2, y2, z2));
    }

    public void jump() {
        this.velocityY = this.jumpVel;
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

    public GridPoint3 getGridPoint3() {
        return new GridPoint3((int) this.x, (int) this.y, (int) this.z);
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
