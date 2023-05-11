package com.ultreon.craft.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.CubeModel;
import com.badlogic.gdx.math.collision.BoundingBox;

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

    public BoundingBox getBoundingBox(int x, int y, int z) {
        Vector3 vector3 = new Vector3(x, y, z);
        return new BoundingBox(vector3, vector3.cpy().add(1));
    }
}
