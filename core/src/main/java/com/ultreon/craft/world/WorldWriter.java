package com.ultreon.craft.world;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;

public interface WorldWriter {
    void set(GridPoint3 pos, Block block);

    void set(int x, int y, int z, Block block);

    void setFast(GridPoint3 pos, Block block);

    void setFast(int x, int y, int z, Block block);
}
