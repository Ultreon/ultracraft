package com.ultreon.craft.world.gen;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;

import java.util.HashMap;
import java.util.Map;

public class FeatureGenData {
    protected Map<GridPoint3, Block> unloadedBlocks = new HashMap<>();
}
