package com.ultreon.craft.world;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Utils;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class Section implements Disposable {
    private final int size;
    private Block[] blocks;
    protected boolean ready;
    protected boolean dirty;
    public final Object lock = new Object();
    private final Vec3i offset;

    public Section(Vec3i offset) {
        this.offset = offset;
        this.size = World.CHUNK_SIZE;
        this.blocks = new Block[this.size * this.size * this.size];
    }

    public Section(Vec3i offset, MapType sectionData) {
        this(offset);

        MapType blocks = sectionData.getMap("Blocks");

        ListType<MapType> paletteData = blocks.getList("Palette");
        List<Block> palette = new ArrayList<>();
        for (MapType mapType : paletteData) {
            palette.add(Registries.BLOCK.getValue(Identifier.parse(mapType.getString("id"))));
        }

        byte[] blockData = blocks.getByteArray("Data");

        for (int i = 0, blockDataLength = blockData.length; i < blockDataLength; i++) {
            int blockIndex = Utils.normalizeToInt(blockData[i]);
            this.blocks[i] = palette.get(blockIndex);
        }
    }

    public MapType save() {
        List<Block> palette = new ArrayList<>();
        ListType<MapType> paletteData = new ListType<>();
        byte[] blockData = new byte[this.size * this.size * this.size];

        for (int i = 0; i < this.blocks.length; i++) {
            Block block = this.blocks[i];
            int blockIndex = palette.indexOf(block);
            if (blockIndex == -1) {
                blockIndex = palette.size();
                palette.add(block);

                MapType mapType = new MapType();
                Identifier key = Registries.BLOCK.getKey(block);
                mapType.putString("id", key == null ? "air" : key.toString());
                paletteData.add(mapType);
            }
            blockData[i] = (byte) blockIndex;
        }

        MapType sectionData = new MapType();

        MapType blocks = new MapType();
        blocks.putByteArray("Data", blockData);
        blocks.put("Palette", paletteData);

        sectionData.put("Blocks", blocks);

        return sectionData;
    }

    public Block get(Vec3i pos) {
        return this.get(pos.x, pos.y, pos.z);
    }

    public Block get(int x, int y, int z) {
        if (x < 0 || x >= this.size) return Blocks.AIR;
        if (y < 0 || y >= this.size) return Blocks.AIR;
        if (z < 0 || z >= this.size) return Blocks.AIR;
        return this.getFast(x, y, z);
    }

    public Block getFast(Vec3i pos) {
        return this.getFast(pos.x, pos.y, pos.z);
    }

    public Block getFast(int x, int y, int z) {
        return this.blocks[this.toIndex(x, y, z)];
    }

    public void set(Vec3i pos, Block block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    public void set(int x, int y, int z, Block block) {
        if (x < 0 || x >= this.size) return;
        if (y < 0 || y >= this.size) return;
        if (z < 0 || z >= this.size) return;
        this.setFast(x, y, z, block);
    }

    public void setFast(Vec3i pos, Block block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    public void setFast(int x, int y, int z, Block block) {
        this.blocks[this.toIndex(x, y, z)] = block;
        this.dirty = true;
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

            this.blocks = null;
        }
    }

    public Vec3i getOffset() {
        return this.offset;
    }

    public boolean isReady() {
        return this.ready;
    }
}
