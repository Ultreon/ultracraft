package com.ultreon.craft.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.CubeModel;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.translations.v0.Language;
import org.jetbrains.annotations.NotNull;

public class Block {
    private static int globalId;
    private final int id;
    private final boolean transparent;
    private final boolean solid;
    private final boolean fluid;
    private final float hardness;
    private final CubeModel model;
    private BakedCubeModel bakedModel;

    public Block(CubeModel model) {
        this(model, new Properties());
    }

    public Block(CubeModel model, Properties properties) {
        this.id = globalId++;
        this.transparent = properties.transparent;
        this.solid = properties.solid;
        this.fluid = properties.fluid;
        this.hardness = properties.hardness;
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
        return this != Blocks.AIR && solid && !fluid;
    }

    public boolean isFluid() {
        return fluid;
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

    public String getTranslation() {
        return Language.translate(getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        Identifier key = Registries.BLOCK.getKey(this);
        return key == null ? "craft/block/air/name" : key.location() + "/block/" + key.path() + "/name";
    }

    public float getHardness() {
        return hardness;
    }

    public static class Properties {
        private float hardness = 0.0F;
        private boolean transparent = false;
        private boolean solid = true;
        private boolean fluid;

        public Properties transparent() {
            this.transparent = true;
            return this;
        }

        public Properties noCollision() {
            this.solid = false;
            return this;
        }

        public Properties hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Properties fluid() {
            this.fluid = true;
            return this;
        }
    }
}
