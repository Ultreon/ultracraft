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
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.model.JsonModel;
import com.ultreon.craft.client.model.JsonModelLoader;
import com.ultreon.craft.client.model.block.BakedCubeModel;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.client.model.item.BlockItemModel;
import com.ultreon.craft.client.model.item.FlatItemModel;
import com.ultreon.craft.client.model.item.ItemModel;
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.Identifier;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class ItemRenderer {
    private final UltracraftClient client;
    private final Environment environment;
    private final ModelBatch batch;
    private final OrthographicCamera itemCam;
    private final Material material;
    private final Quaternion quaternion = new Quaternion();
    private final Vector3 rotation = new Vector3(-30, 45, 0);
    private final Vector3 position = new Vector3(0, 0, -100);
    private final Vector3 scale = new Vector3(20, 20, 20);
    protected final Vector3 tmp = new Vector3();
    private final Map<Item, ItemModel> models = new HashMap<>();
    private final Map<Item, ModelInstance> modelsInstances = new HashMap<>();

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
                this.renderModel(modelInstance, renderer, x + 8, this.client.getScaledHeight() - y - 16);
                return;
            }

            this.renderBlockItem(blockItem.getBlock(), renderer, x + 8, this.client.getScaledHeight() - y - 16);
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

    private void renderModel(ModelInstance instance, Renderer renderer, int x, int y) {
        if (instance != null) {
            renderer.external(() -> {
                float guiScale = this.client.getGuiScale();
                this.itemCam.zoom = 4.0f / guiScale;
                this.itemCam.far = 100000;
                this.itemCam.update();
                this.batch.begin(this.itemCam);
                instance.transform.set(this.position.cpy().add((x - (int) (this.client.getScaledWidth() / 2.0F)) * guiScale, (y - (int) (this.client.getScaledHeight() / 2.0F)) * guiScale, 100), this.quaternion, this.scale);
                instance.transform.rotate(Vector3.X, this.rotation.x);
                instance.transform.rotate(Vector3.Y, this.rotation.y);
                this.batch.render(instance, environment);
                this.batch.end();
            });
        }
    }

    private void renderBlockItem(Block block, Renderer renderer, int x, int y) {
        renderer.external(() -> {
            float guiScale = this.client.getGuiScale();
            this.itemCam.zoom = 4.0f / guiScale;
            this.itemCam.far = 100000;
            this.itemCam.update();
            BakedCubeModel bakedBlockModel = this.client.getBakedBlockModel(block);
            this.batch.begin(this.itemCam);
            Mesh mesh = bakedBlockModel.getMesh();
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
        });
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
        this.modelsInstances.put(blockItem, new ModelInstance(models.get(blockItem).getModel()));
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

    public void registerModels(JsonModelLoader loader) {
        Registries.ITEM.getValues().forEach((e) -> {
            try {
                JsonModel load = loader.load(e);
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
            this.registerBlockModel(blockItem, () -> this.client.getBakedBlockModel(blockItem.getBlock()));
        } else {

        }
    }
}
