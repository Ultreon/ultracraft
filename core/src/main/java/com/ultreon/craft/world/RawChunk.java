package com.ultreon.craft.world;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.gen.TreeData;
import org.jetbrains.annotations.NotNull;

import static com.ultreon.craft.world.World.CHUNK_SIZE;
import static com.ultreon.craft.world.World.WORLD_DEPTH;

public abstract class RawChunk implements WorldRW {
    public final GridPoint3 offset = new GridPoint3();
    protected final World world;
    protected final ChunkPos pos;
    protected final Object lock = new Object();
    public TreeData treeData;
    public boolean modifiedByPlayer;
    protected Block[] blocks;
    protected final Heightmap heightmap;
    public final int size;
    public final int height;
    protected final int topOffset;
    protected final int bottomOffset;
    protected final int leftOffset;
    protected final int rightOffset;
    protected final int frontOffset;
    protected final int backOffset;
    protected final int sizeTimesHeight;

    public RawChunk(World world, int size, int height, ChunkPos pos) {
        this(world, size, height, pos, new Block[size * height * size], new Heightmap());
    }

    protected RawChunk(World world, int size, int height, ChunkPos pos, Block[] blocks, Heightmap heightmap) {
        this.offset.set(pos.x * CHUNK_SIZE, WORLD_DEPTH, pos.z * CHUNK_SIZE);
        this.world = world;
        this.pos = pos;
        this.blocks = blocks;
        this.heightmap = heightmap;
        this.size = size;
        this.height = height;
        this.topOffset = size * size;
        this.bottomOffset = -size * size;
        this.leftOffset = -1;
        this.rightOffset = 1;
        this.frontOffset = -size;
        this.backOffset = size;
        this.sizeTimesHeight = size * size;
    }

    @Override
    @NotNull
    public Block get(GridPoint3 pos) {
        return get(pos.x, pos.y, pos.z);
    }

    @Override
    @NotNull
    public Block get(int x, int y, int z) {
        if (x < 0 || x >= size) return Blocks.AIR;
        if (y < 0 || y >= height) return Blocks.AIR;
        if (z < 0 || z >= size) return Blocks.AIR;
        return getFast(x, y, z);
    }

    @Override
    @NotNull
    public Block getFast(GridPoint3 pos) {
        return getFast(pos.x, pos.y, pos.z);
    }

    @Override
    @NotNull
    public Block getFast(int x, int y, int z) {
        Block block = blocks[x + z * size + y * sizeTimesHeight];
        if (block == null) block = Blocks.AIR;
        return block;
    }

    @Override
    public void set(GridPoint3 pos, Block block) {
        set(pos.x, pos.y, pos.z, block);
    }

    @Override
    public void set(int x, int y, int z, Block block) {
        if (x < 0 || x >= size) return;
        if (y < 0 || y >= height) return;
        if (z < 0 || z >= size) return;
        setFast(x, y, z, block);
    }

    @Override
    public void setFast(GridPoint3 pos, Block block) {
        set(pos.x, pos.y, pos.z, block);
    }

    @Override
    public void setFast(int x, int y, int z, Block block) {
        if (block == null) block = Blocks.AIR;

        this.blocks[x + z * this.size + y * this.sizeTimesHeight] = block;
        if (!block.isAir()) {
            if (this.heightmap.get(x, z) < y) {
                this.heightmap.set(x, z, y);
            }
        } else if (this.heightmap.get(x, z) <= y) {
            int curY = y - 1;
            while (true) {
                Block cur = getFast(x, curY, z);
                if (!cur.isAir() || curY <= WORLD_DEPTH) break;
                curY--;
            }
            this.heightmap.set(x, z, curY);
        }
    }

    public void dispose() {
        this.blocks = null;
        this.heightmap.dispose();
    }
}
