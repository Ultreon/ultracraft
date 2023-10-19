package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.ClientSectionData;
import com.ultreon.craft.collection.PaletteContainer;
import com.ultreon.craft.render.world.ChunkMesh;
import com.ultreon.data.Types;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.*;

public class Section implements Disposable {
    public static final List<TextureRegion> BREAK_TEX = new ArrayList<>();
    final Map<Vec3i, Float> breaking = new HashMap<>();
    private final int size;
    public final Vector3 renderOffset = new Vector3();
    public final Vector3 translation = new Vector3();
    @ClientOnly
    public ChunkMesh chunkMesh;
    protected boolean ready;
    protected boolean dirty;
    public final Object lock = new Object();
    final Vec3i offset;
    private final PaletteContainer<MapType, Block> paletteContainer = new PaletteContainer<>(4096, Types.MAP, Block::load);
    private boolean disposed;
    private static long disposeCount;
    private ClientSectionData clientData = new ClientSectionData();

    public Section(Vec3i offset) {
        this.offset = offset;
        this.size = World.CHUNK_SIZE;
    }

    public static long getDisposeCount() {
        return Section.disposeCount;
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

    float getBreakProgress(float x, float y, float z) {
        Vec3i pos = new Vec3i((int) x, (int) y, (int) z);
        Float v = this.breaking.get(pos);
        if (v != null) {
            return v;
        }
        return -1.0F;
    }

    public void startBreaking(int x, int y, int z) {
        this.breaking.put(new Vec3i(x, y, z), 0.0F);
    }

    public void stopBreaking(int x, int y, int z) {
        this.breaking.remove(new Vec3i(x, y, z));
    }

    public void continueBreaking(int x, int y, int z, float amount) {
        Vec3i pos = new Vec3i(x, y, z);
        Float v = this.breaking.computeIfPresent(pos, (pos1, cur) -> Mth.clamp(cur + amount, 0, 1));
        if (v != null && v == 1.0F) {
            this.set(new Vec3i(x, y, z), Blocks.AIR);
        }
    }

    @Override
    public void dispose() {
        synchronized (this.lock) {
            this.disposed = true;
            this.ready = false;
            this.paletteContainer.dispose();
            Section.disposeCount++;
        }
    }

    public Vec3i getOffset() {
        return this.offset;
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public ClientSectionData getClientData() {
        return this.clientData;
    }

    public Map<Vec3i, Float> getBreaking() {
        return Collections.unmodifiableMap(this.breaking);
    }
}
