package com.ultreon.craft.world;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public interface WorldReader {
    Block get(Vec3i pos);

    Block get(int x, int y, int z);

    Block getFast(Vec3i pos);

    Block getFast(int x, int y, int z);
}
