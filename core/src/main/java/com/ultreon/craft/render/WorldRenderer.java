package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.gui.GuiContainer;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.Section;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.ultreon.craft.world.Chunk.VERTEX_SIZE;
import static com.ultreon.craft.world.World.*;

public class WorldRenderer implements RenderableProvider {
    protected Material material;
    private final Map<Section, SectionRenderInfo> renderInfoMap = new ConcurrentHashMap<>();
    private final List<Section> toRemove = new CopyOnWriteArrayList<>();
    private int renderedChunks;
    private int totalChunks;

    private final short[] indices;
    private final World world;
    private final UltreonCraft game = UltreonCraft.get();
    private final Executor renderExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1));
    private List<Section> renderSchedule = new ArrayList<>();

    public WorldRenderer(World world) {
        this.world = world;

        int len = World.CHUNK_SIZE * World.CHUNK_SIZE * World.CHUNK_SIZE * 6 * 6 / 3;

        this.indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            this.indices[i] = j;
            this.indices[i + 1] = (short) (j + 1);
            this.indices[i + 2] = (short) (j + 2);
            this.indices[i + 3] = (short) (j + 2);
            this.indices[i + 4] = (short) (j + 3);
            this.indices[i + 5] = j;
        }

        this.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.game.blocksTextureAtlas.getTexture()));
        this.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
    }

    public void unloadChunk(Chunk chunk) {
        for (Section section : chunk.getSections()) {
            this.toRemove.add(section);
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        this.renderedChunks = 0;
        List<Chunk> chunks = this.world.getChunks();
        this.totalChunks = chunks.size();
        List<Chunk> toSort = new ArrayList<>(chunks);
        Player player = this.game.player;
        if (player == null) return;
        toSort.sort((o1, o2) -> {
            Vec3d mid1 = new Vec3d(o1.getOffset().x + (float) CHUNK_SIZE / 2, o1.getOffset().y + (float) CHUNK_HEIGHT / 2, o1.getOffset().z + (float) CHUNK_SIZE / 2);
            Vec3d mid2 = new Vec3d(o2.getOffset().x + (float) CHUNK_SIZE / 2, o2.getOffset().y + (float) CHUNK_HEIGHT / 2, o2.getOffset().z + (float) CHUNK_SIZE / 2);
            return Double.compare(mid2.dst(player.getPosition()), mid1.dst(player.getPosition()));
        });

        for (Section section : this.toRemove) {
            SectionRenderInfo removed = this.renderInfoMap.remove(section);
            removed.dispose();
        }

        boolean renderedNewChunk = false;
        for (Chunk chunk : toSort) {
            if (!chunk.isReady()) continue;

            for (Section section : chunk.getSections()) {
                if (section.scheduledRender) continue;
                Vec3i sectionOffset = section.getOffset();
                Vec3f offset = sectionOffset.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();

                synchronized (section.lock) {
                    if (!section.isReady()) continue;

                    SectionRenderInfo info = this.renderInfoMap.get(section);
                    if (info == null) {
                        info = new SectionRenderInfo();
                        this.renderInfoMap.put(section, info);
                    }

                    Mesh mesh = info.mesh;
                    if (section.isDirty() || mesh == null) {
                        if (section.scheduledRender) continue;
                        section.scheduledRender = true;
                        this.renderSchedule.add(0, section);
                        continue;
                    }

                    if (info.numVertices == 0) continue;

                    Renderable piece = pool.obtain();

                    piece.material = this.material;
                    piece.meshPart.mesh = mesh;
                    piece.meshPart.offset = 0;
                    piece.meshPart.size = info.numVertices;
                    piece.meshPart.primitiveType = GL20.GL_TRIANGLES;
                    piece.worldTransform.setToTranslation(offset.x, offset.y, offset.z);

                    renderables.add(piece);
                    this.renderedChunks = this.renderedChunks + 1;
                }
            }
        }

        Section section = this.renderSchedule.remove(0);
        if (section != null) {
            SectionRenderInfo info = this.renderInfoMap.get(section);

            if (info.mesh != null) info.mesh.dispose();

            SectionRenderInfo finalInfo = info;
            FloatList vertices = new FloatArrayList();
            int numVertices = this.buildVertices(section, vertices);

            finalInfo.mesh = new Mesh(false, false, numVertices,
                    this.indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
            finalInfo.mesh.setIndices(this.indices);
            finalInfo.numVertices = numVertices / 4 * 6;
            finalInfo.mesh.setVertices(vertices.toFloatArray());
            vertices.clear();
            section.setDirty(false);

            section.scheduledRender = false;
        }
    }

    /** Creates a mesh out of the chunk, returning the number of indices produced
     * @return the number of vertices produced */
    protected int buildVertices(Section section, FloatList vertices) {
        int i = 0;
        Vec3i offset = new Vec3i();
        
        for (int y = 0; y < CHUNK_SIZE; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = section.get(x, y, z);

                    if (block == Blocks.AIR) continue;

                    BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

                    if (model == null) continue;

                    if (y < CHUNK_SIZE - 1) {
                        if (this.getB(section, x, y + 1, z) == Blocks.AIR || this.getB(section, x, y + 1, z).isTransparent()) createTop(offset, x, y, z, model.top(), vertices);
                    } else {
                        createTop(offset, x, y, z, model.top(), vertices);
                    }
                    if (y > 0) {
                        if (this.getB(section, x, y - 1, z) == Blocks.AIR || this.getB(section, x, y - 1, z).isTransparent()) createBottom(offset, x, y, z, model.bottom(), vertices);
                    } else {
                        createBottom(offset, x, y, z, model.bottom(), vertices);
                    }
                    if (x > 0) {
                        if (this.getB(section, x - 1, y, z) == Blocks.AIR || this.getB(section, x - 1, y, z).isTransparent()) createLeft(offset, x, y, z, model.left(), vertices);
                    } else {
                        createLeft(offset, x, y, z, model.left(), vertices);
                    }
                    if (x < CHUNK_SIZE - 1) {
                        if (this.getB(section, x + 1, y, z) == Blocks.AIR || this.getB(section, x + 1, y, z).isTransparent()) createRight(offset, x, y, z, model.right(), vertices);
                    } else {
                        createRight(offset, x, y, z, model.right(), vertices);
                    }
                    if (z > 0) {
                        if (this.getB(section, x, y, z - 1) == Blocks.AIR || this.getB(section, x, y, z - 1).isTransparent()) createFront(offset, x, y, z, model.front(), vertices);
                    } else {
                        createFront(offset, x, y, z, model.front(), vertices);
                    }
                    if (z < CHUNK_SIZE - 1) {
                        if (this.getB(section, x, y, z + 1) == Blocks.AIR || this.getB(section, x, y, z + 1).isTransparent()) createBack(offset, x, y, z, model.back(), vertices);
                    } else {
                        createBack(offset, x, y, z, model.back(), vertices);
                    }
                }
            }
        }
        return vertices.size() / VERTEX_SIZE + 1;
    }

    private Block getB(Section section, int x, int y, int z) {
//		return world.get(new Vec3i(pos.x * size + x, y, pos.z * size + z));
        return section.get(new Vec3i(x, y, z));
    }

    protected static void createTop(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
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

    protected static void createBottom(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
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

    protected static void createLeft(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
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

    protected static void createRight(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
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

    protected static void createFront(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
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

    protected static void createBack(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
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

    private static class SectionRenderInfo {
        public Mesh mesh;
        public int numVertices;
        
        public void dispose() {
            if (this.mesh != null) {
                this.mesh.dispose();
            }
        }
    }

    public int getRenderedChunks() {
        return this.renderedChunks;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public World getWorld() {
        return this.world;
    }
}
