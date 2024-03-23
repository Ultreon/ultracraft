package com.ultreon.craft.client.item;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.model.block.BakedCubeModel;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.client.model.block.BlockModelRegistry;
import com.ultreon.craft.client.model.item.BlockItemModel;
import com.ultreon.craft.client.model.item.FlatItemModel;
import com.ultreon.craft.client.model.item.ItemModel;
import com.ultreon.craft.client.model.model.Json5Model;
import com.ultreon.craft.client.model.model.Json5ModelLoader;
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ItemRenderer {
    private final UltracraftClient client;
    private final Environment environment;
    private final ModelBatch batch;
    private final OrthographicCamera itemCam;
    private final Material material;
    private final Quaternion quaternion = new Quaternion();
    private final Vector3 rotation = new Vector3(-30, 45, 0);
    private final Vector3 position = new Vector3(0, 0, -1000);
    private final Vector3 scale = new Vector3(20, 20, 20);
    protected final Vector3 tmp = new Vector3();
    private final Map<Item, ItemModel> models = new HashMap<>();
    private final Map<Item, ModelInstance> modelsInstances = new HashMap<>();
    private Cache<BlockMetadata, ModelInstance> blockModelCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

    public ItemRenderer(UltracraftClient client) {
        this.client = client;
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
        this.environment.add(new DirectionalLight().set(.8f, .8f, .8f, this.tmp.set(.8f, 0, -.6f).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(.8f, .8f, .8f, this.tmp.set(-.8f, 0, .6f).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, this.tmp.set(0, -1, 0).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(0.17f, .17f, .17f, this.tmp.set(0, 1, 0).rotate(Vector3.Y, 45)));
        this.batch = new ModelBatch();
        this.itemCam = new OrthographicCamera(client.getScaledWidth(), client.getScaledHeight());
        this.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.client.blocksTextureAtlas.getTexture()));
        this.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
    }

    public void render(Item item, Renderer renderer, int x, int y) {
        if (item == null || item == Items.AIR) {
            return;
        }

        if (item instanceof BlockItem blockItem) {
            ModelInstance modelInstance = modelsInstances.get(item);
            if (modelInstance != null) {
                this.renderModel(modelInstance, models.get(item), renderer, x + 8, this.client.getScaledHeight() - y - 16);
                return;
            }

            this.renderBlockItem(blockItem,blockItem.createBlockMeta(), renderer, x + 8, this.client.getScaledHeight() - y - 16);
            return;
        }

        Identifier curKey = Registries.ITEM.getId(item);
        if (curKey == null) {
            renderer.blitColor(Color.WHITE);
            renderer.blit((TextureRegion) null, x, y, 16, 16);
        } else {
            TextureRegion texture = this.client.itemTextureAtlas.get(curKey.mapPath(path -> "textures/items/" + path + ".png"));
            renderer.blitColor(Color.WHITE);
            renderer.blit(texture, x, y, 16, 16);
        }
    }

    private void renderModel(ModelInstance instance, ItemModel itemModel, Renderer renderer, int x, int y) {
        if (instance != null) {
            renderer.external(() -> {
                float guiScale = this.client.getGuiScale();
                this.itemCam.zoom = 4.0f / guiScale;
                this.itemCam.far = 100000;
                this.itemCam.update();
                this.batch.begin(this.itemCam);
                Vector3 scl = this.scale.cpy().scl(itemModel.getScale());
                instance.transform.idt().translate(this.position.cpy().add((x - (int) (this.client.getScaledWidth() / 2.0F)) * guiScale, (y - (int) (this.client.getScaledHeight() / 2.0F)) * guiScale, 100)).translate(itemModel.getOffset().scl(1 / scl.x, 1 / scl.y, 1 / scl.z)).scale(scl.x, scl.y, scl.z);
                instance.transform.rotate(Vector3.X, this.rotation.x);
                instance.transform.rotate(Vector3.Y, this.rotation.y);
                this.batch.render(instance, environment);
                this.batch.end();
            });
        }
    }

    private void renderBlockItem(Item item, BlockMetadata block, Renderer renderer, int x, int y) {
        renderer.external(() -> {
            float guiScale = this.client.getGuiScale();
            this.itemCam.zoom = 4.0f / guiScale;
            this.itemCam.far = 100000;
            this.itemCam.update();
            @NotNull BlockModel blockModel = this.client.getBakedBlockModel(block);
            if (blockModel == BakedCubeModel.DEFAULT) {
                renderCustomBlock(item, block, renderer, x, y);
                return;
            }
            if (blockModel instanceof BakedCubeModel bakedModel) {
                this.batch.begin(this.itemCam);
                Mesh mesh = bakedModel.getMesh();
                Renderable renderable = new Renderable();
                renderable.meshPart.mesh = mesh;
                renderable.meshPart.center.set(0F, 0F, 0F);
                renderable.meshPart.offset = 0;
                renderable.meshPart.size = mesh.getMaxVertices();
                renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
                renderable.material = this.material;
                renderable.environment = this.environment;
                renderable.worldTransform.set(this.position.cpy().add((x - (int) (this.client.getScaledWidth() / 2.0F)) * guiScale, -(-y + (int) (this.client.getScaledHeight() / 2.0F)) * guiScale, 100), this.quaternion, this.scale);
                renderable.worldTransform.rotate(Vector3.X, this.rotation.x);
                renderable.worldTransform.rotate(Vector3.Y, this.rotation.y);
                this.batch.render(renderable);
                this.batch.end();
            } else {
                try {
                    ModelInstance modelInstance = this.blockModelCache.get(block, () -> new ModelInstance(blockModel.getModel()));
                    this.batch.render(modelInstance, this.environment);
                } catch (ExecutionException e) {
                    UltracraftClient.LOGGER.warn("Error occurred while caching block model:", e);
                }
            }
        });
    }

    private void renderCustomBlock(Item item, BlockMetadata block, Renderer renderer, int x, int y) {
        ModelInstance modelInstance = new ModelInstance(getModel(block));
        this.modelsInstances.put(item, modelInstance);

        renderModel(modelInstance, models.get(item), renderer, x, y);
    }

    private static Model getModel(BlockMetadata block) {
        BlockModel blockModel = BlockModelRegistry.get(block);
        Model defaultModel = BakedCubeModel.DEFAULT.getModel();
        if (blockModel == null) return defaultModel;
        Model model = blockModel.getModel();
        return model == null ? defaultModel : model;
    }

    public OrthographicCamera getItemCam() {
        return this.itemCam;
    }

    public void resize(int width, int height) {
        this.itemCam.viewportWidth = width / this.client.getGuiScale();
        this.itemCam.viewportHeight = height / this.client.getGuiScale();
        this.itemCam.update(true);
    }

    public ModelInstance createModelInstance(ItemStack stack) {
        return new ModelInstance(models.get(stack.getItem()).getModel());
    }

    public void registerModel(Item item, ItemModel model) {
        models.put(item, model);
    }

    public void registerBlockModel(BlockItem blockItem, Supplier<BlockModel> model) {
        models.put(blockItem, new BlockItemModel(Suppliers.memoize(model::get)));
        ItemModel itemModel = models.get(blockItem);
        Vector3 scale = itemModel.getScale();
        ModelInstance value = new ModelInstance(itemModel.getModel());
        value.transform.scale(scale.x, scale.y, scale.z);
        value.calculateTransforms();
        this.modelsInstances.put(blockItem, value);
    }

    public void loadModels(UltracraftClient client) {
        for (Map.Entry<Item, ItemModel> e : models.entrySet()) {
            Item item = e.getKey();
            ItemModel value = e.getValue();
            value.load(client);

            Model model = value.getModel();
            this.modelsInstances.put(item, new ModelInstance(model));
        }
    }

    public void registerModels(Json5ModelLoader loader) {
        Registries.ITEM.values().forEach((e) -> {
            try {
                if (e instanceof BlockItem blockItem) {
                    this.registerBlockModel(blockItem, () -> this.client.getBakedBlockModel(blockItem.createBlockMeta()));
                    return;
                }

                Json5Model load = loader.load(e);
                if (load == null) {
                    fallbackModel(e);
                    return;
                }

                this.registerModel(e, Objects.requireNonNullElseGet(load, () -> new FlatItemModel(e)));
            } catch (IOException ex) {
                fallbackModel(e);
            }
        });
    }

    private void fallbackModel(Item e) {
        if (e instanceof BlockItem blockItem) {
//            this.registerBlockModel(blockItem, () -> this.client.getBakedBlockModel(blockItem.createBlockMeta()));
        }
    }

    public void reload() {
        this.modelsInstances.clear();
    }
}
