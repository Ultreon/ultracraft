package com.ultreon.craft.world;

import com.ultreon.craft.util.Vec3i;

public final class BlockPos extends Vec3i {
    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlockPos) obj;
        return this.x == that.x &&
                this.y == that.y &&
                this.z == that.z;
    }

    @Override
    public String toString() {
        return "(%d, %d, %d)".formatted(x, y, z);
    }

    public BlockPos below() {
        return new BlockPos(x, y-1, z);
    }

    public BlockPos above() {
        return new BlockPos(x, y+1, z);
    }

    public BlockPos relative(int off) {
        return new BlockPos(x+off, y+off, z+off);
    }

    public BlockPos relative(int offX, int offY, int offZ) {
        return new BlockPos(x+offX, y+offY, z+offZ);
    }
}
