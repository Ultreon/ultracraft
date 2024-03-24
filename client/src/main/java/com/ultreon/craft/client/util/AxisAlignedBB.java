package com.ultreon.craft.client.util;

import com.badlogic.gdx.math.Vector3;

public class AxisAlignedBB {
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;
    private final Vector3 tmp1 = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final Vector3 tmp3 = new Vector3();
    private final Vector3 tmp4 = new Vector3();

    public AxisAlignedBB(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean intersects(AxisAlignedBB other) {
        return this.maxX >= other.minX && this.minX <= other.maxX
            && this.maxY >= other.minY && this.minY <= other.maxY
            && this.maxZ >= other.minZ && this.minZ <= other.maxZ;
    }

    public AxisAlignedBB offset(float x, float y, float z) {
        return new AxisAlignedBB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    @Deprecated(forRemoval = true)
    public Vector3 calculateIntersect(AxisAlignedBB other, Vector3 direction) {
        return calculateIntersect(other, direction, new Vector3());
    }

    public Vector3 calculateIntersect(AxisAlignedBB other, Vector3 direction, Vector3 output) {
        Vector3 minDistance = other.min(tmp1).sub(this.max(tmp2));
        Vector3 maxDistance = other.max(tmp3).sub(this.min(tmp4));

        // Calculate the overlap distance along each axis
        float overlapX = direction.x == 0 ? 0 : (direction.x > 0 ? maxDistance.x : minDistance.x) / direction.x;
        float overlapY = direction.y == 0 ? 0 : (direction.y > 0 ? maxDistance.y : minDistance.y) / direction.y;
        float overlapZ = direction.z == 0 ? 0 : (direction.z > 0 ? maxDistance.z : minDistance.z) / direction.z;

        // Find the smallest overlap distance and return the intersection vector
        float smallestOverlap = Math.min(Math.min(overlapX, overlapY), overlapZ);
        return output.set(smallestOverlap * direction.x, smallestOverlap * direction.y, smallestOverlap * direction.z);
    }

    private Vector3 min(Vector3 tmp) {
        return tmp.set(minX, minY, minZ);
    }

    private Vector3 max(Vector3 tmp) {
        return tmp.set(maxX, maxY, maxZ);
    }
}