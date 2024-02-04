package com.ultreon.craft.block;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.tool.ToolType;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.loot.ConstantLoot;
import com.ultreon.craft.world.loot.LootGenerator;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Block implements DataWriter<MapType> {
    private final boolean transparent;
    private final boolean collides;
    private final boolean fluid;
    private final boolean toolRequired;
    private final float hardness;
    @Nullable
    private final ToolType effectiveTool;
    private final LootGenerator lootGen;
    private final boolean disableRendering;
    private final boolean hasCustomRender;
    private final boolean replaceable;
    private final boolean occlude;
    private final boolean greedyMerge;

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
        this.lootGen = properties.loot;
        this.replaceable = properties.replaceable;
        this.hasCustomRender = properties.hasCustomRender;
        this.occlude = properties.occlude;
        this.greedyMerge = properties.greedyMerge;
    }

    public ElementID getId() {
        ElementID key = Registries.BLOCK.getKey(this);
        return key == null ? new ElementID(CommonConstants.NAMESPACE, "air") : key;
    }

    public boolean isAir() {
        return this == Blocks.AIR || this == Blocks.CAVE_AIR;
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
        ElementID id = ElementID.tryParse(data.getString("id"));
        if (id == null) return Blocks.AIR;
        Block block = Registries.BLOCK.getValue(id);
        return block == null ? Blocks.AIR : block;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeId(this.getId());
    }

    public TextObject getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    @NotNull
    public String getTranslationId() {
        ElementID key = Registries.BLOCK.getKey(this);
        return key == null ? "ultracraft.block.air.name" : key.namespace() + ".block." + key.path() + ".name";
    }

    public float getHardness() {
        return this.hardness;
    }

    public final boolean isUnbreakable() {
        return Float.isInfinite(this.hardness);
    }

    @Nullable
    public ToolType getEffectiveTool() {
        return this.effectiveTool;
    }

    public boolean isToolRequired() {
        return this.toolRequired;
    }

    public LootGenerator getLootGen() {
        return this.lootGen;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + this.getId() +
                '}';
    }

    public boolean doesOcclude() {
        return this.occlude;
    }

    public boolean shouldGreedyMerge() {
        return greedyMerge;
    }

    public boolean hasCustomRender() {
        return this.hasCustomRender;
    }

    public int getRawId() {
        return Registries.BLOCK.getId(this);
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public static class Properties {
        private boolean greedyMerge = true;
        private boolean replaceable;
        private boolean hasCustomRender;
        @Nullable
        private ToolType effectiveTool = null;
        private float hardness = 0.0F;
        private boolean transparent = false;
        private boolean solid = true;
        private boolean fluid = false;
        private boolean requiresTool = false;
        private LootGenerator loot = ConstantLoot.EMPTY;
        private boolean disableRendering;
        private boolean occlude;

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
            this.loot = new ConstantLoot(drops);
            return this;
        }

        public @This Properties dropsItems(Item...  drops) {
            this.loot = new ConstantLoot(Arrays.stream(drops).map(Item::defaultStack).toList());
            return this;
        }

        public @This Properties dropsItems(LootGenerator drops) {
            this.loot = drops;
            return this;
        }

        public @This Properties noRendering() {
            this.disableRendering = true;
            return this;
        }

        public @This Properties usesCustomRender() {
            this.hasCustomRender = true;
            return this;
        }

        public @This Properties instaBreak() {
            this.hardness = 0;
            return this;
        }

        public @This Properties unbreakable() {
            this.hardness = Float.POSITIVE_INFINITY;
            return this;
        }

        public @This Properties replaceable() {
            this.replaceable = true;
            return this;
        }

        public @This Properties noOcclude() {
            this.occlude = false;
            return this;
        }

        public @This Properties noGreedyMerge() {
            this.greedyMerge = false;
            return this;
        }
    }
}
