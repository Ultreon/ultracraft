package com.ultreon.craft.render.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.client.ClientSectionData;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.Section;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public final class WorldRenderer implements RenderableProvider {
    private final ChunkMeshBuilder meshBuilder;
    private final Material material;
    private int visibleChunks;
    private int loadedChunks;

    private final World world;
    private final UltreonCraft game = UltreonCraft.get();

    private static long freeMeshes;
    private static long poolFree;
    private static int poolPeak;
    private static int poolMax;
    private final FlushablePool<ChunkMesh> pool = new FlushablePool<>() {
        @Override
        protected ChunkMesh newObject() {
            return new ChunkMesh();
        }
    };

    public WorldRenderer(World world) {
        this.world = world;

        int len = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 6 / 3;

        short[] indices = new short[len];
        short j = 0;

        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short) (j + 1);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = (short) (j + 2);
            indices[i + 4] = (short) (j + 3);
            indices[i + 5] = j;
        }

        this.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.game.blocksTextureAtlas.getTexture()));
        this.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
        this.meshBuilder = new ChunkMeshBuilder(indices);
    }

    @Override
    public void getRenderables(final Array<Renderable> output, final Pool<Renderable> ignored) {
        var player = this.game.player;
        if (player == null) return;

        output.clear();

        var chunks = WorldRenderer.sortChunks(this.world.getLoadedChunks(), player);
        this.loadedChunks = chunks.size();
        this.visibleChunks = 0;

        for (var chunk : chunks) {
            if (!chunk.isReady()) continue;

            for (Section section : chunk.getSections()) {
                if (!section.isReady()) continue;

                Vec3i sectionOffset = section.getOffset();
                Vec3f offset = sectionOffset.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();
                section.renderOffset.set(offset.x, offset.y, offset.z);

                ChunkMesh chunkMesh = this.pool.obtain();

                this.freeOldMesh(chunkMesh);

                if (section.chunkMesh == null && !section.isDisposed()) {
                    section.chunkMesh = chunkMesh;
                    chunkMesh.section = section;
                }

                this.meshBuilder.buildMesh(chunkMesh, section);

                chunkMesh.section = section;
                chunkMesh.renderable.material = this.material;
                chunkMesh.meshPart.primitiveType = GL20.GL_TRIANGLES;
                chunkMesh.transform.setToTranslation(offset.x, offset.y, offset.z);

                output.add(chunkMesh.renderable);

                this.visibleChunks++;
            }

            this.doPoolStatistics();
        }
    }

    private void doPoolStatistics() {
        WorldRenderer.poolFree = this.pool.getFree();
        WorldRenderer.poolPeak = this.pool.peak;
        WorldRenderer.poolMax = this.pool.max;
    }

    private void putMeshData(ChunkMesh chunkMesh, Section section, ClientSectionData clientData, Vec3f offset) {
    }

    private void freeOldMesh(ChunkMesh chunkMesh) {
        final Section oldSection = chunkMesh.section;
        if (oldSection != null && oldSection.isDisposed()) {
            oldSection.chunkMesh = null;
            this.pool.free(chunkMesh);

            WorldRenderer.freeMeshes++;
        }
    }

    @NotNull
    private static List<Chunk> sortChunks(Collection<Chunk> chunks, Player player) {
        List<Chunk> toSort = new ArrayList<>(chunks);
        toSort.sort((o1, o2) -> {
            Vec3d mid1 = new Vec3d(o1.getOffset().x + (float) CHUNK_SIZE, o1.getOffset().y + (float) CHUNK_HEIGHT, o1.getOffset().z + (float) CHUNK_SIZE);
            Vec3d mid2 = new Vec3d(o2.getOffset().x + (float) CHUNK_SIZE, o2.getOffset().y + (float) CHUNK_HEIGHT, o2.getOffset().z + (float) CHUNK_SIZE);
            return Double.compare(mid2.dst(player.getPosition()), mid1.dst(player.getPosition()));
        });
        return toSort;
    }

    public static Matrix4 rotateTowards(Matrix4 transformMatrix, Vector3 currentPos, Vector3 targetPos) {
        // Calculate the direction vector from current to target position
        Vector3 direction = targetPos.cpy().sub(currentPos).nor();

        // Calculate the rotation angle in radians using the direction vector
        float angle = (float) Math.atan2(-direction.z, -direction.x);

        // Create a rotation matrix using LibGDX's Matrix4 API
        transformMatrix.rotateRad(Vector3.Y, angle);
        return transformMatrix;
    }

    public int getVisibleChunks() {
        return this.visibleChunks;
    }

    public int getLoadedChunks() {
        return this.loadedChunks;
    }

    public static long getPoolFree() {
        return WorldRenderer.poolFree;
    }

    public static int getPoolPeak() {
        return WorldRenderer.poolPeak;
    }

    public static int getPoolMax() {
        return WorldRenderer.poolMax;
    }

    public static long getFreeMeshes() {
        return WorldRenderer.freeMeshes;
    }

    public World getWorld() {
        return this.world;
    }

    public void dispose() {
        this.pool.clear();
        this.pool.flush();
    }
}
