package com.ultreon.craft.client.registry;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.model.entity.renderer.EntityRenderer;
import com.ultreon.craft.client.resources.ContextAwareReloadable;
import com.ultreon.craft.resources.ReloadContext;
import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

public class EntityRendererManager implements ContextAwareReloadable, Disposable {
    public static final Map<EntityType<?>, BiFunction<EntityModel<?>, Model, EntityRenderer<?>>> REGISTRY = new HashMap<>();
    public final Map<EntityType<?>, EntityRenderer<?>> finishedRegistry = new OrderedMap<>();
    private final EntityModelManager modelManager;

    public EntityRendererManager(EntityModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity, M extends EntityModel<T>> void register(EntityType<@NotNull T> entityType, BiFunction<M, Model, EntityRenderer<T>> factory) {
        REGISTRY.put(entityType, (entityModel, model) -> factory.apply((M) entityModel, model));
    }

    public void load() {
    }

    public <T extends Entity> EntityRenderer<?> get(EntityType<@NotNull T> entityType) {
        return this.finishedRegistry.get(entityType);
    }

    @Override
    public void dispose() {
        for (EntityRenderer<?> renderer : this.finishedRegistry.values()) {
            renderer.dispose();
        }
    }

    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        for (EntityRenderer<?> renderer : this.finishedRegistry.values()) {
            context.submit(renderer::dispose);
        }

        this.finishedRegistry.clear();

        for (var entry : REGISTRY.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            Model finished = modelManager.getFinished(entityType);
            if (finished == null)
                continue;

            EntityRenderer<?> renderer = entry.getValue().apply(this.modelManager.get(entityType), finished);
            this.finishedRegistry.put(entityType, renderer);
        }

        for (var e : EntityRendererManager.REGISTRY.entrySet()) {
            UltracraftClient.LOGGER.debug("Registering renderer for entity {}", e.getKey().getId());
            if (this.finishedRegistry.containsKey(e.getKey())) {
                UltracraftClient.LOGGER.warn("Renderer for entity {} is already registered", e.getKey().getId());
                continue;
            }

            if (this.modelManager.get(e.getKey()) == null) {
                UltracraftClient.LOGGER.warn("Model for entity {} is null", e.getKey().getId());
                continue;
            }

            if (this.modelManager.getFinished(e.getKey()) == null) {
                UltracraftClient.LOGGER.warn("Finished model for entity {} is null", e.getKey().getId());
                continue;
            }

            this.finishedRegistry.put(e.getKey(), e.getValue().apply(this.modelManager.get(e.getKey()), this.modelManager.getFinished(e.getKey())));
        }

        // Iterate through all entity types and register their models and renderers
        for (var e : Registries.ENTITY_TYPE.entries()) {
            EntityType<?> type = e.getValue();
            EntityRenderer<?> renderer = this.get(type);
            EntityModel<?> entityModel = this.modelManager.get(type);

            Identifier key = e.getKey().element();
            FileHandle handle = UltracraftClient.resource(key.mapPath(path -> "models/entity/" + path + ".g3dj"));

            // Load and register the model if it exists
            if (handle.exists()) {
                Model model = UltracraftClient.invokeAndWait(() -> this.modelManager.modelLoader.loadModel(handle, fileName -> {
                    String filePath = fileName.substring(("assets/" + key.namespace() + "/models/entity/").length());
                    return new Texture(UltracraftClient.resource(key.mapPath(path -> "textures/entity/" + filePath)));
                }));
                if (model == null)
                    throw new RuntimeException("Failed to load entity model: " + key.mapPath(path -> "models/entity/" + path + ".g3dj"));
                // Set blending and alpha test attributes for the model materials
                model.materials.forEach(modelModel -> {
                    modelModel.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
                    modelModel.set(FloatAttribute.createAlphaTest(0.5f));
                });
                this.modelManager.registerFinished(type, model);
            } else {
                // If the model does not exist, use the entity model and renderer
                if (entityModel == null) {
                    UltracraftClient.LOGGER.warn("Model not found for entity {}", type.getId());
                    continue;
                }

                if (renderer == null) {
                    UltracraftClient.LOGGER.warn("Renderer not found for entity {}", type.getId());
                    continue;
                }

                this.modelManager.registerFinished(type, entityModel.finish(renderer.getTextures()));
            }
        }
    }
}
