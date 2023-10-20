package com.ultreon.craft.block;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.item.tool.ToolType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Block implements DataWriter<MapType> {
    private final boolean transparent;
    private final boolean collides;
    private final boolean fluid;
    private final boolean requiresTool;
    private final float hardness;
    private final ToolType effectiveTool;

    public Block() {
        this(new Properties());
    }

    public Block(Properties properties) {
        this.transparent = properties.transparent;
        this.collides = properties.solid;
        this.fluid = properties.fluid;
        this.hardness = properties.hardness;
        this.effectiveTool = properties.effectiveTool;
        this.requiresTool = properties.requiresTool;
    }

    public Identifier id() {
        Identifier key = Registries.BLOCK.getKey(this);
        return key == null ? UltreonCraft.id("air") : key;
    }

    public boolean isAir() {
        return this == Blocks.AIR;
    }

    public boolean hasCollider() {
        return !this.isAir() && this.collides;
    }

    public boolean isFluid() {
        return this.fluid;
    }

    public BoundingBox getBoundingBox(int x, int y, int z) {
        return new BoundingBox(new Vec3d(x, y, z), new Vec3d(x + 1, y + 1, z + 1));
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public BoundingBox getBoundingBox(Vec3i posNext) {
        return this.getBoundingBox(posNext.x, posNext.y, posNext.z);
    }

    @Override
    public MapType save() {
        MapType data = new MapType();
        data.putString("Id", this.id().toString());
        return data;
    }

    public static Block load(MapType data) {
        Identifier id = Identifier.tryParse(data.getString("Id"));
        if (id == null) return Blocks.AIR;
        Block block = Registries.BLOCK.getValue(id);
        return block == null ? Blocks.AIR : block;
    }

    public String getTranslation() {
        return Language.translate(getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        Identifier key = Registries.BLOCK.getKey(this);
        return key == null ? "craft.block.air.name" : key.location() + ".block." + key.path() + ".name";
    }

    public float getHardness() {
        return this.hardness;
    }

    @Nullable
    public ToolType getEffectiveTool() {
        return this.effectiveTool;
    }

    public boolean getRequiresTool() {
        return this.requiresTool;
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
