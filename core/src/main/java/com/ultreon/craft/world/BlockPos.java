package com.ultreon.craft.world;

import com.badlogic.gdx.math.Vector3;
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
}
