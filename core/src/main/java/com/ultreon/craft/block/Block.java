package com.ultreon.craft.block;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.item.tool.ToolType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Block {
    private static int globalId;
    private final int id;
    private final boolean transparent;
    private final boolean solid;
    private final boolean fluid;
    private final boolean requiresTool;
    private final float hardness;
    @Deprecated
    private final CubeModel model;
    @Deprecated
    private BakedCubeModel bakedModel;
    private final ToolType effectiveTool;

    public Block() {
        this(new Properties());
    }

    public Block(Properties properties) {
        this.id = globalId++;
        this.transparent = properties.transparent;
        this.solid = properties.solid;
        this.fluid = properties.fluid;
        this.hardness = properties.hardness;
        this.effectiveTool = properties.effectiveTool;
        this.requiresTool = properties.requiresTool;
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
        this.fluid = properties.fluid;
        this.hardness = properties.hardness;
        this.effectiveTool = properties.effectiveTool;
        this.requiresTool = properties.requiresTool;
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
        return this != Blocks.AIR && solid;
    }

    public boolean isFluid() {
        return fluid;
    }

    public BoundingBox getBoundingBox(int x, int y, int z) {
        return new BoundingBox(new Vec3d(x, y, z), new Vec3d(x + 1, y + 1, z + 1));
    }

    public boolean isTransparent() {
        return transparent;
    }

    public BoundingBox getBoundingBox(Vec3i posNext) {
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

    @Nullable
    public ToolType getEffectiveTool() {
        return effectiveTool;
    }

    public boolean getRequiresTool() {
        return requiresTool;
    }

    public static class Properties {
        private ToolType effectiveTool = null;
        private float hardness = 0.0F;
        private boolean transparent = false;
        private boolean solid = true;
        private boolean fluid = false;
        private boolean requiresTool = false;

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

        public Properties effectiveTool(ToolType toolType) {
            this.effectiveTool = toolType;
            return this;
        }

        public Properties requiresTool() {
            this.requiresTool = true;
            return this;
        }

        public Properties fluid() {
            this.fluid = true;
            return this;
        }
    }
}
