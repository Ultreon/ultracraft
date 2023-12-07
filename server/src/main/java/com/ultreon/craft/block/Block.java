package com.ultreon.craft.block;

import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.tool.ToolType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Block implements DataWriter<MapType> {
    private final boolean transparent;
    private final boolean collides;
    private final boolean fluid;
    private final boolean toolRequired;
    private final float hardness;
    @Nullable
    private final ToolType effectiveTool;
    private final List<ItemStack> itemDrops;
    private final boolean disableRendering;

    public Block() {
        this(new Properties());
    }

    public Block(Properties properties) {
        this.transparent = properties.transparent;
        this.disableRendering = properties.disableRendering;
        this.collides = properties.solid;
        this.fluid = properties.fluid;
        this.hardness = properties.hardness;
        this.effectiveTool = properties.effectiveTool;
        this.toolRequired = properties.requiresTool;
        this.itemDrops = Collections.unmodifiableList(properties.itemDrops);
    }

    public Identifier getId() {
        Identifier key = Registries.BLOCK.getKey(this);
        return key == null ? new Identifier(Identifier.getDefaultNamespace(), "air") : key;
    }

    public boolean isAir() {
        return this == Blocks.AIR;
    }

    public boolean hasCollider() {
        return !this.isAir() && this.collides;
    }

    public boolean doesRender() {
        return !this.isAir() && !this.disableRendering;
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
        data.putString("id", this.getId().toString());
        return data;
    }

    public static Block load(MapType data) {
        Identifier id = Identifier.tryParse(data.getString("id"));
        if (id == null) return Blocks.AIR;
        Block block = Registries.BLOCK.getValue(id);
        return block == null ? Blocks.AIR : block;
    }

    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        Identifier key = Registries.BLOCK.getKey(this);
        return key == null ? "ultracraft.block.air.name" : key.location() + ".block." + key.path() + ".name";
    }

    public float getHardness() {
        return this.hardness;
    }

    @Nullable
    public ToolType getEffectiveTool() {
        return this.effectiveTool;
    }

    public boolean isToolRequired() {
        return this.toolRequired;
    }

    public List<ItemStack> getItemDrops() {
        return this.itemDrops;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + this.getId() +
                '}';
    }

    public boolean hasOcclusion() {
        return true;
    }

    public boolean shouldGreedyMerge() {
        return true;
    }

    public boolean hasCustomRender() {
        return false;
    }

    public int getRegIdx() {
        return Registries.BLOCK.values().indexOf(this);
    }

    public static class Properties {
        @Nullable
        private ToolType effectiveTool = null;
        private float hardness = 0.0F;
        private boolean transparent = false;
        private boolean solid = true;
        private boolean fluid = false;
        private boolean requiresTool = false;
        private final List<ItemStack> itemDrops = new ArrayList<>();
        private boolean disableRendering;

        public @This Properties transparent() {
            this.transparent = true;
            return this;
        }

        public @This Properties noCollision() {
            this.solid = false;
            return this;
        }

        public @This Properties hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public @This Properties effectiveTool(ToolType toolType) {
            this.effectiveTool = toolType;
            return this;
        }

        public @This Properties requiresTool() {
            this.requiresTool = true;
            return this;
        }

        public @This Properties fluid() {
            this.fluid = true;
            return this;
        }

        public @This Properties dropsItems(ItemStack...  drops) {
            this.itemDrops.addAll(List.of(drops));
            return this;
        }

        public @This Properties dropsItems(Item...  drops) {
            this.itemDrops.addAll(Arrays.stream(drops).map(Item::defaultStack).toList());
            return this;
        }

        public @This Properties noRendering() {
            this.disableRendering = true;
            return this;
        }
    }
}
