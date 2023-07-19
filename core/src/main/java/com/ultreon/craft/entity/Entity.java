package com.ultreon.craft.entity;

import com.badlogic.gdx.math.GridPoint2;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.BoundingBoxUtils;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;

import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Entity {
    private final EntityType<? extends Entity> type;
    protected final World world;
    protected double x;
    protected double y;
    protected double z;
    public float xRot;
    public float yRot;
    private int id = -1;
    public boolean onGround;
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public float gravity = 0.08F;
    public boolean noGravity;
    public boolean isColliding;
    public boolean isCollidingX;
    public boolean isCollidingY;
    public boolean isCollidingZ;

    public boolean noClip;
    protected float fallDistance = 0;
    private int age;
    private boolean deletionPending = false;

    public Entity(EntityType<? extends Entity> entityType, World world) {
        this.type = entityType;
        this.world = world;
    }

    public static @NotNull Entity loadFrom(World world, MapType data) {
        Identifier typeId = Identifier.parse(data.getString("type"));
        EntityType<?> type = Registries.ENTITIES.getValue(typeId);
        Entity entity = type.create(world);

        entity.id = data.getInt("id");

        entity.loadWithPos(data);
        return entity;
    }

    public void loadWithPos(MapType data) {
        MapType position = data.getMap("Position", new MapType());
        this.x = position.getDouble("x", this.x);
        this.y = position.getDouble("y", this.y);
        this.z = position.getDouble("z", this.z);

        this.load(data);
    }

    public void load(MapType data) {
        MapType rotation = data.getMap("Rotation", new MapType());
        this.xRot = rotation.getFloat("x", this.xRot);
        this.yRot = rotation.getFloat("y", this.yRot);

        MapType velocity = data.getMap("Velocity", new MapType());
        this.velocityX = velocity.getDouble("x", this.velocityX);
        this.velocityY = velocity.getDouble("y", this.velocityY);
        this.velocityZ = velocity.getDouble("z", this.velocityZ);

        this.fallDistance = data.getFloat("fallDistance", this.fallDistance);
        this.gravity = data.getFloat("gravity", this.gravity);
        this.age = data.getInt("age", this.age);
        this.noGravity = data.getBoolean("noGravity", this.noGravity);
        this.noClip = data.getBoolean("noClip", this.noClip);
    }


    public MapType save(MapType data) {
        MapType position = new MapType();
        position.putDouble("x", this.x);
        position.putDouble("y", this.y);
        position.putDouble("z", this.z);
        data.put("Position", position);

        MapType rotation = new MapType();
        rotation.putFloat("x", this.xRot);
        rotation.putFloat("y", this.yRot);
        data.put("Rotation", rotation);

        MapType velocity = new MapType();
        velocity.putDouble("x", this.velocityX);
        velocity.putDouble("y", this.velocityY);
        velocity.putDouble("z", this.velocityZ);
        data.put("Velocity", velocity);

        data.putInt("id", this.id);
        data.putString("type", Objects.requireNonNull(Registries.ENTITIES.getKey(this.type)).toString());

        data.putFloat("fallDistance", this.fallDistance);
        data.putFloat("gravity", this.gravity);
        data.putInt("age", this.age);
        data.putBoolean("noGravity", this.noGravity);
        data.putBoolean("noClip", this.noClip);

        return data;
    }

    public EntitySize getSize() {
        return this.type.getSize();
    }

    public void tick() {
        if (!this.noGravity) {
            this.velocityY -= this.gravity;
        }

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        this.velocityX *= 0.6F;
        this.velocityY *= 0.98F;
        this.velocityZ *= 0.6F;

        if (this.onGround) {
            this.velocityX *= 0.9f;
            this.velocityZ *= 0.9f;
        }

        this.age++;
    }

    public void move(double dx, double dy, double dz) {
        double oDx = dx;
        double oDy = dy;
        double oDz = dz;
        BoundingBox ext = this.getBoundingBox();
        if (dx < 0) ext.min.x += dx;
        else ext.max.x += dx;
        if (dy < 0) ext.min.y += dy;
        else ext.max.y += dy;
        if (dz < 0) ext.min.z += dz;
        else ext.max.z += dz;
        ext.update();
        if (this.noClip) {
            this.x += dx;
            this.y += dy;
            this.z += dz;
        } else {
            List<BoundingBox> boxes = this.world.collide(ext);
            BoundingBox pBox = this.getBoundingBox();
            this.isColliding = false;
            this.isCollidingY = false;
            for (BoundingBox box : boxes) {
                double dy2 = BoundingBoxUtils.clipYCollide(box, pBox, dy);
                this.isColliding |= dy != dy2;
                this.isCollidingY |= dy != dy2;
                dy = dy2;
            }
            pBox.min.add(0.0f, dy, 0.0f);
            pBox.max.add(0.0f, dy, 0.0f);
            pBox.update();
            this.isCollidingX = false;
            for (BoundingBox box : boxes) {
                double dx2 = BoundingBoxUtils.clipXCollide(box, pBox, dx);
                this.isColliding |= dx != dx2;
                this.isCollidingX |= dx != dx2;
                dx = dx2;
            }
            pBox.min.add(dx, 0.0f, 0.0f);
            pBox.max.add(dx, 0.0f, 0.0f);
            pBox.update();
            this.isCollidingZ = false;
            for (BoundingBox box : boxes) {
                double dz2 = BoundingBoxUtils.clipZCollide(box, pBox, dz);
                this.isColliding |= dz != dz2;
                this.isCollidingZ |= dz != dz2;
                dz = dz2;
            }
            pBox.min.add(0.0f, 0.0f, dz);
            pBox.max.add(0.0f, 0.0f, dz);
            pBox.update();
            this.onGround = oDy != dy && oDy < 0.0f;
            if (oDx != dx) {
                this.velocityX = 0.0f;
            }

            if (dy >= 0) this.fallDistance = 0.0F;

            if (oDy != dy) {
                this.hitGround();
                this.fallDistance = 0.0F;
                this.velocityY = 0.0f;
            } else if (dy < 0) {
                this.fallDistance -= dy;
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

    public boolean isInVoid() {
        return this.y < World.WORLD_DEPTH - 64;
    }

    public BoundingBox getBoundingBox() {
        return this.getBoundingBox(this.getSize());
    }

    @ApiStatus.OverrideOnly
    public BoundingBox getBoundingBox(EntitySize size) {
        double x1 = this.x - size.width() / 2;
        double y1 = this.y;
        double z1 = this.z - size.width() / 2;
        double x2 = this.x + size.width() / 2;
        double y2 = this.y + size.height();
        double z2 = this.z + size.width() / 2;
        return new BoundingBox(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2));
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getXRot() {
        return this.xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public float getYRot() {
        return this.yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = Mth.clamp(yRot, -90, 90);
    }

    public Vec3d getPosition() {
        return new Vec3d(this.x, this.y, this.z);
    }

    @Deprecated
    public void setPosition(Vector3 position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public void setPosition(Vec3d position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i blockPosition() {
        return new Vec3i((int) this.x, (int) this.y, (int) this.z);
    }

    public Vector2 getRotation() {
        return new Vector2(this.xRot, this.yRot);
    }

    public Vec3d getLookVector() {
        // Calculate the direction vector
        Vec3d direction = new Vec3d();
        float yRot = Mth.clamp(this.yRot, -89.9F, 89.9F);
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

    public Vec3d getVelocity() {
        return new Vec3d(this.velocityX, this.velocityY, this.velocityZ);
    }

    public void setVelocity(Vec3d velocity) {
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
    }

    @ApiStatus.OverrideOnly
    public void onPrepareSpawn(MapType spawnData) {

    }

    public World getWorld() {
        return this.world;
    }

    public int getId() {
        return this.id;
    }

    @ApiStatus.Internal
    public void setId(int id) {
        this.id = id;
    }

    public float getGravity() {
        return this.gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public int getAge() {
        return this.age;
    }

    public void rotate(GridPoint2 rotation) {
        this.xRot = this.xRot + rotation.x;
        this.yRot = Mth.clamp(this.yRot + rotation.y, -90, 90);
    }

    public void deferDeletion() {
        this.deletionPending = true;
    }

    public boolean isDeletionPending() {
        return this.deletionPending;
    }

    public EntityType<? extends Entity> getType() {
        return this.type;
    }
}
