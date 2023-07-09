package com.ultreon.craft.block;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public class Block implements DataWriter<MapType> {
    private static int globalId;
    private final int id;
    private final boolean transparent;
    private final boolean solid;
    @Deprecated
    private final CubeModel model;
    @Deprecated
    private BakedCubeModel bakedModel;
    private boolean blockLight;

    public Block() {
        this(new Properties());
    }

    public Block(Properties properties) {
        this.id = globalId++;
        this.transparent = properties.transparent;
        this.blockLight = properties.blockLight;
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

    public Identifier id() {
        return Registries.BLOCK.getKey(this);
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
        return new BoundingBox(new Vec3d(x, y, z), new Vec3d(x + 1, y + 1, z + 1));
    }

    public boolean isTransparent() {
        return transparent;
    }

    public BoundingBox getBoundingBox(Vec3i posNext) {
        return getBoundingBox(posNext.x, posNext.y, posNext.z);
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

    public boolean blocksLight() {
        return this.blockLight;
    }

    public static class Properties {
        private boolean transparent = false;
        private boolean blockLight = true;
        private boolean solid = true;

        public Properties transparent() {
            this.transparent = true;
            return this;
        }

        public Properties passLight() {
            this.blockLight = false;
            return this;
        }

        public Properties noCollision() {
            this.solid = false;
            return this;
        }
    }
}
