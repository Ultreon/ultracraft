package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.checkerframework.common.reflection.qual.NewInstance;

public record BlockPos(int x, int y, int z) {
    public BlockPos(double x, double y, double z) {
        this((int) x, (int) y, (int) z);
    }

    public BlockPos() {
        this(0, 0, 0);
    }

    public BlockPos(Vec3i vec) {
        this(vec.x, vec.y, vec.z);
    }

    @NewInstance
    public BlockPos offset(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    public Vec3i vec() {
        return new Vec3i(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "%d,%d,%d".formatted(this.x, this.y, this.z);
    }
}
