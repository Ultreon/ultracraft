package com.ultreon.craft.block;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.util.AxisAlignedBB;

public class Block {
    private final int id;
    private final CubeModel model;
    private BakedCubeModel bakedModel;

    public Block(int id, CubeModel model) {
        this.id = id;
        this.model = model;
    }

    public CubeModel getModel() {
        return model;
    }

    public BakedCubeModel bakedModel() {
        return bakedModel;
    }

    public byte id() {
        return (byte) id;
    }

    public void bake(Texture texture) {
        if (this == Blocks.AIR) return;
        this.bakedModel = model.bake(texture);
    }

    public boolean isAir() {
        return this == Blocks.AIR;
    }

    public boolean isSolid() {
        return this != Blocks.AIR;
    }

    public AxisAlignedBB getBoundingBox(int x, int y, int z) {
        return new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
    }
}
