package com.ultreon.craft.client.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.client.ShaderContext;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.client.registry.BlockEntityModelRegistry;
import com.ultreon.craft.client.render.meshing.GreedyMesher;
import com.ultreon.craft.collection.Storage;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.util.PosOutOfBoundsException;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.libs.commons.v0.Mth;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public final class ClientChunk extends Chunk {
    final GreedyMesher mesher;
    private final ClientWorld clientWorld;
    public Vector3 renderOffset = new Vector3();
    public ChunkMesh solidMesh;
    public ChunkMesh transparentMesh;
    public volatile boolean dirty;
    public boolean initialized = false;
    private final UltracraftClient client = UltracraftClient.get();
    private final Map<BlockPos, Block> customRendered = new HashMap<>();
    private final Map<BlockPos, ModelInstance> models = new HashMap<>();
    private Vector3 tmp = new Vector3();
    private Vector3 tmp1 = new Vector3();

    /**
     * @deprecated Use {@link #ClientChunk(ClientWorld, ChunkPos, Storage, Storage, Map)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public ClientChunk(ClientWorld world, int ignoredSize, int ignoredHeight, ChunkPos pos, Storage<Block> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities) {
        this(world, pos, storage, biomeStorage, blockEntities);
    }

    public ClientChunk(ClientWorld world, ChunkPos pos, Storage<Block> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities) {
        super(world, pos, storage, biomeStorage);
        this.clientWorld = world;
        this.active = false;

        blockEntities.forEach((blockPos, type) -> {
            if (type != null) {
                this.setBlockEntity(blockPos, type.create(world, blockPos));
            }
        });

        this.mesher = new GreedyMesher(this, true);
    }

    public float getLightLevel(int x, int y, int z) throws PosOutOfBoundsException {
        if(this.isOutOfBounds(x, y, z))
            throw new PosOutOfBoundsException();

        int sunlight = this.lightMap.getSunlight(x, y, z);
        int blockLight = this.lightMap.getBlockLight(x, y, z);
        float sunlightMapped = Chunk.lightLevelMap[Mth.clamp(sunlight, 0, Chunk.MAX_LIGHT_LEVEL)] - Chunk.lightLevelMap[0];
        float blockLightMapped = Chunk.lightLevelMap[Mth.clamp(blockLight, 0, Chunk.MAX_LIGHT_LEVEL)] - Chunk.lightLevelMap[0];

        return Mth.clamp(sunlightMapped + blockLightMapped + Chunk.lightLevelMap[0], Chunk.lightLevelMap[0], Chunk.lightLevelMap[Chunk.MAX_LIGHT_LEVEL]);
    }

    @Override
    public void dispose() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        synchronized (this) {
            super.dispose();

            WorldRenderer worldRenderer = UltracraftClient.get().worldRenderer;
            if ((this.solidMesh != null || this.transparentMesh != null) && worldRenderer != null) {
                worldRenderer.free(this);
            }
        }
    }

    @Override
    public Block getFast(int x, int y, int z) {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        return super.getFast(x, y, z);
    }

    @Override
    public boolean setFast(int x, int y, int z, Block block) {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        boolean isBlockSet = super.setFast(x, y, z, block);

        this.dirty = true;
        this.clientWorld.updateChunkAndNeighbours(this);
        return isBlockSet;
    }

    public void updated() {
        this.dirty = false;
    }

    @Override
    public void onUpdated() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        super.onUpdated();
    }

    @Override
    public ClientWorld getWorld() {
        return this.clientWorld;
    }

    void ready() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }
        this.ready = true;
        this.clientWorld.updateChunkAndNeighbours(this);
    }

    public Object getBounds() {
        return null;
    }

    public Map<BlockPos, Block> getCustomRendered() {
        return this.customRendered;
    }

    @Override
    protected void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
        super.setBlockEntity(blockPos, blockEntity);

        System.out.println("blockPos = " + blockPos);

        BlockModel blockModel = BlockEntityModelRegistry.get(blockEntity.getType());
        if (blockModel != null) {
            blockModel.loadInto(blockEntity.pos(), this);
        } else {
            UltracraftClient.LOGGER.warn("No block entity model for " + blockEntity.getType().getId() + " at " + blockPos);
        }
    }

    @CanIgnoreReturnValue
    public ModelInstance addModel(BlockPos pos, ModelInstance instance) {
        return this.models.put(pos, instance);
    }

    public void renderModels(Array<Renderable> output, Pool<Renderable> renderablePool) {
        for (Map.Entry<BlockPos, ModelInstance> entry : this.models.entrySet()) {
            ModelInstance value = entry.getValue();
            if (value == null) continue;

            BlockPos key = entry.getKey();

            float x = (float) key.x() % 16;
            float z = (float) key.z() % 16;
            if (x < 0) x += 16;
            if (z < 0) z += 16;
            value.materials.forEach(attributes -> {
                float sunlight = clientWorld.getGlobalSunlight();
                if (attributes.has(ColorAttribute.Ambient)) {
                    ((ColorAttribute)attributes.get(ColorAttribute.Ambient)).color.set(sunlight, sunlight, sunlight, 1);
                } else {
                    attributes.set(new ColorAttribute(ColorAttribute.Ambient, sunlight, sunlight, sunlight, 1));
                }
            });
            value.userData = Shaders.MODEL_VIEW;
            value.transform.setToTranslationAndScaling(this.tmp.set(this.renderOffset).add(x, (float) key.y() % 65536, z), this.tmp1.set(1 / 16f, 1 / 16f, 1 / 16f));
            value.getRenderables(output, renderablePool);
        }
    }

    public void loadCustomRendered() {
        for (BlockEntity blockEntity : getBlockEntities()) {
            BlockModel blockModel = BlockEntityModelRegistry.get(blockEntity.getType());
            if (blockModel != null) {
                blockModel.loadInto(blockEntity.pos(), this);
            }
        }
    }

    @ApiStatus.Internal
    public void whileLocked(Runnable func) {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        synchronized (this) {
            func.run();
        }
    }

    public UltracraftClient getClient() {
        return client;
    }
}
