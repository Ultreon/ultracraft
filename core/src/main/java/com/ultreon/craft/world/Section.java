package com.ultreon.craft.world;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteContainer;
import com.ultreon.data.Types;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

public class Section implements Disposable {
    private final int size;
    public volatile boolean scheduledRender;
    protected boolean ready;
    protected boolean dirty;
    public final Object lock = new Object();
    private final Vec3i offset;
    private final PaletteContainer<MapType, Block> paletteContainer = new PaletteContainer<>(4096, Types.MAP, Block::load);

    public Section(Vec3i offset) {
        this.offset = offset;
        this.size = World.CHUNK_SIZE;
    }

    public Section(Vec3i offset, MapType sectionData) {
        this(offset);

        this.paletteContainer.load(sectionData.getMap("Blocks"));
    }

    public MapType save() {
        MapType data = new MapType();
        data.put("Blocks", this.paletteContainer.save());
        return data;
    }

    public Block get(Vec3i pos) {
        return this.get(pos.x, pos.y, pos.z);
    }

    public Block get(int x, int y, int z) {
        if (this.isOutOfBounds(x, y, z)) return Blocks.AIR;
        return this.getFast(x, y, z);
    }

    public Block getFast(Vec3i pos) {
        return this.getFast(pos.x, pos.y, pos.z);
    }

    public Block getFast(int x, int y, int z) {
        Block block = this.paletteContainer.get(this.toIndex(x, y, z));
        return block == null ? Blocks.AIR : block;
    }

    public void set(Vec3i pos, Block block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    public void set(int x, int y, int z, Block block) {
        if (this.isOutOfBounds(x, y, z)) return;
        this.setFast(x, y, z, block);
    }

    public void setFast(Vec3i pos, Block block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    public void setFast(int x, int y, int z, Block block) {
        this.paletteContainer.set(this.toIndex(x, y, z), block);
        this.dirty = true;
    }

    private boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= this.size || y < 0 || y >= this.size || z < 0 || z >= this.size;
    }

    private int toIndex(int x, int y, int z) {
        return x + z * this.size + y * (this.size * this.size);
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void dispose() {
        synchronized (this.lock) {
            this.ready = false;
        }
    }

    public Vec3i getOffset() {
        return this.offset;
    }

    public boolean isReady() {
        return this.ready;
    }
}
