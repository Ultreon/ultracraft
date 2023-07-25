package com.ultreon.craft.world;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public interface WorldWriter {
    void set(Vec3i pos, Block block);

    void set(int x, int y, int z, Block block);

    void setFast(Vec3i pos, Block block);

    void setFast(int x, int y, int z, Block block);
}
