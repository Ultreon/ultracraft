package com.ultreon.craft.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.CubeModel;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Block {
    private static int globalId;
    private final int id;
    private final boolean transparent;
    private final boolean solid;
    private final CubeModel model;
    private BakedCubeModel bakedModel;

    public Block(CubeModel model) {
        this(model, new Properties());
    }

    public Block(CubeModel model, Properties properties) {
        this.id = globalId++;
        this.transparent = properties.transparent;
        this.solid = properties.solid;
        this.model = model;
    }

    public CubeModel getModel() {
        return model;
    }

    public BakedCubeModel bakedModel() {
        return bakedModel;
    }

    @Deprecated
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
        return new BoundingBox(new Vector3(x, y, z), new Vector3(x + 1, y + 1, z + 1));
    }

    public boolean isTransparent() {
        return transparent;
    }

    public BoundingBox getBoundingBox(GridPoint3 posNext) {
        return getBoundingBox(posNext.x, posNext.y, posNext.z);
    }

    public static class Properties {
        private boolean transparent = false;
        private boolean solid = true;

        public Properties transparent() {
            this.transparent = true;
            return this;
        }

        public Properties noCollision() {
            this.solid = false;
            return this;
        }
    }
}
