package com.ultreon.craft.util;

public class AxisAlignedBB {
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;

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
}