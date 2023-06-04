package com.ultreon.craft.world;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;

public interface WorldReader {
    Block get(GridPoint3 pos);

    Block get(int x, int y, int z);

    Block getFast(GridPoint3 pos);

    Block getFast(int x, int y, int z);
}
