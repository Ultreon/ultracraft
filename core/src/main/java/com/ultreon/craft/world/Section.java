package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.util.Utils;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.ArrayList;
import java.util.List;

import static com.ultreon.craft.world.Chunk.VERTEX_SIZE;

public class Section implements Disposable {
    private final int size;
    private Block[] blocks;
    protected boolean ready;
    protected boolean dirty;
    protected Mesh mesh;
    protected Material material;
    protected int numVertices;
    protected final Object lock = new Object();
    private final GridPoint3 offset;

    public Section(GridPoint3 offset) {
        this.offset = offset;
        this.size = World.CHUNK_SIZE;
        this.blocks = new Block[this.size * this.size * this.size];
    }

    public Section(GridPoint3 offset, MapType sectionData) {
        this(offset);

        MapType blocks = sectionData.getMap("Blocks");

        ListType<MapType> paletteData = blocks.getList("Palette");
        List<Block> palette = new ArrayList<>();
        for (var mapType : paletteData) {
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
            var block = this.blocks[i];
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

    public Block get(GridPoint3 pos) {
        return this.get(pos.x, pos.y, pos.z);
    }

    public Block get(int x, int y, int z) {
        if (x < 0 || x >= this.size) return Blocks.AIR;
        if (y < 0 || y >= this.size) return Blocks.AIR;
        if (z < 0 || z >= this.size) return Blocks.AIR;
        return this.getFast(x, y, z);
    }

    public Block getFast(GridPoint3 pos) {
        return this.getFast(pos.x, pos.y, pos.z);
    }

    public Block getFast(int x, int y, int z) {
        return this.blocks[this.toIndex(x, y, z)];
    }

    public void set(GridPoint3 pos, Block block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    public void set(int x, int y, int z, Block block) {
        if (x < 0 || x >= this.size) return;
        if (y < 0 || y >= this.size) return;
        if (z < 0 || z >= this.size) return;
        this.setFast(x, y, z, block);
    }

    public void setFast(GridPoint3 pos, Block block) {
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

    /** Creates a mesh out of the chunk, returning the number of indices produced
     * @return the number of vertices produced */
    protected int buildVertices(FloatList vertices) {
        int i = 0;
        for (int y = 0; y < this.size; y++) {
            for (int z = 0; z < this.size; z++) {
                for (int x = 0; x < this.size; x++, i++) {
                    Block block = this.get(x, y, z);

                    if (block == null || block == Blocks.AIR) continue;

                    BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

                    if (model == null) continue;

                    if (y < this.size - 1) {
                        if (this.getB(x, y + 1, z) == null || this.getB(x, y + 1, z) == Blocks.AIR || this.getB(x, y + 1, z).isTransparent()) createTop(this.offset, x, y, z, model.top(), vertices);
                    } else {
                        createTop(this.offset, x, y, z, model.top(), vertices);
                    }
                    if (y > 0) {
                        if (this.getB(x, y - 1, z) == null || this.getB(x, y - 1, z) == Blocks.AIR || this.getB(x, y - 1, z).isTransparent()) createBottom(this.offset, x, y, z, model.bottom(), vertices);
                    } else {
                        createBottom(this.offset, x, y, z, model.bottom(), vertices);
                    }
                    if (x > 0) {
                        if (this.getB(x - 1, y, z) == null || this.getB(x - 1, y, z) == Blocks.AIR || this.getB(x - 1, y, z).isTransparent()) createLeft(this.offset, x, y, z, model.left(), vertices);
                    } else {
                        createLeft(this.offset, x, y, z, model.left(), vertices);
                    }
                    if (x < this.size - 1) {
                        if (this.getB(x + 1, y, z) == null || this.getB(x + 1, y, z) == Blocks.AIR || this.getB(x + 1, y, z).isTransparent()) createRight(this.offset, x, y, z, model.right(), vertices);
                    } else {
                        createRight(this.offset, x, y, z, model.right(), vertices);
                    }
                    if (z > 0) {
                        if (this.getB(x, y, z - 1) == null || this.getB(x, y, z - 1) == Blocks.AIR || this.getB(x, y, z - 1).isTransparent()) createFront(this.offset, x, y, z, model.front(), vertices);
                    } else {
                        createFront(this.offset, x, y, z, model.front(), vertices);
                    }
                    if (z < this.size - 1) {
                        if (this.getB(x, y, z + 1) == null || this.getB(x, y, z + 1) == Blocks.AIR || this.getB(x, y, z + 1).isTransparent()) createBack(this.offset, x, y, z, model.back(), vertices);
                    } else {
                        createBack(this.offset, x, y, z, model.back(), vertices);
                    }
                }
            }
        }
        return vertices.size() / VERTEX_SIZE + 1;
    }

    private Block getB(int x, int y, int z) {
//		return world.get(new GridPoint3(pos.x * size + x, y, pos.z * size + z));
        return this.get(new GridPoint3(x, y, z));
    }

    protected static void createTop(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    protected static void createBottom(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    protected static void createLeft(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    protected static void createRight(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    protected static void createFront(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    protected static void createBack(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    @Override
    public void dispose() {
        synchronized (this.lock) {
            this.ready = false;

            this.blocks = null;
            if (this.mesh != null) {
                UltreonCraft.get().runLater(new Task(new Identifier("mesh_disposal"), this.mesh::dispose));
            }
            this.material = null;
            this.mesh = null;
        }
    }
}
