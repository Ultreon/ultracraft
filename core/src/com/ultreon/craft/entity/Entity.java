package com.ultreon.craft.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.entity.util.EntitySize;
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

    public Entity(EntityType<? extends Entity> entityType, World world) {
        this.type = entityType;
        this.world = world;
    }

    public EntitySize getSize() {
        return type.getSize();
    }

    public void tick() {

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

    public Vector2 getRotation() {
        return new Vector2(xRot, yRot);
    }

    public Vector3 getLookVector() {
        // Calculate the direction vector
        Vector3 direction = new Vector3();
        var yRot = Mth.clamp(this.yRot, -89.9F, 90);
        direction.x = MathUtils.cosDeg(yRot) * MathUtils.sinDeg(xRot);
        direction.z = MathUtils.cosDeg(yRot) * MathUtils.cosDeg(xRot);
        direction.y = MathUtils.sinDeg(yRot);

        // Normalize the direction vector
        direction.nor();
        return direction;
    }

    public void setRotation(Vector2 position) {
        this.xRot = position.x;
        this.yRot = Mth.clamp(position.y, -90, 90);
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
}
