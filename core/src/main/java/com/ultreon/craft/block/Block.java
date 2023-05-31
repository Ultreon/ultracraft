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
    @Deprecated
    private final CubeModel model;
    @Deprecated
    private BakedCubeModel bakedModel;

    public Block() {
        this(new Properties());
    }

    public Block(Properties properties) {
        this.id = globalId++;
        this.transparent = properties.transparent;
        this.solid = properties.solid;
        this.model = null;
    }

    @Deprecated
    public Block(CubeModel model) {
        this(model, new Properties());
    }

    @Deprecated
    public Block(CubeModel model, Properties properties) {
        this.id = globalId++;
        this.transparent = properties.transparent;
        this.solid = properties.solid;
        this.model = model;
    }

    @Deprecated
    public CubeModel getModel() {
        return model;
    }

    @Deprecated
    public BakedCubeModel bakedModel() {
        return bakedModel;
    }

    @Deprecated
    public byte id() {
        return (byte) id;
    }

    @Deprecated
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

    public String getTranslation() {
        return Language.translate(getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        Identifier key = Registries.BLOCK.getKey(this);
        return key == null ? "craft/block/air/name" : key.location() + "/block/" + key.path() + "/name";
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
