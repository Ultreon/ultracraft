package com.ultreon.craft.client.registry;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.api.events.ClientRegistrationEvents;
import com.ultreon.craft.client.model.blockbench.BlockBenchModelImporter;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.resources.ContextAwareReloadable;
import com.ultreon.craft.resources.ReloadContext;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityModelManager implements ContextAwareReloadable {
    private final Map<EntityType<?>, EntityModel<?>> registry = new HashMap<>();
    private final Map<EntityType<?>, Model> finishedRegistry = new HashMap<>();
    final ModelLoader<ModelLoader.ModelParameters> modelLoader;
    private final UltracraftClient client;

    public EntityModelManager(ModelLoader<ModelLoader.ModelParameters> modelLoader, UltracraftClient client) {
        this.modelLoader = modelLoader;
        this.client = client;
    }

    public <T extends Entity> void register(EntityType<@NotNull T> entityType, EntityModel<T> model) {
        this.registry.put(entityType, model);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Entity> EntityModel<T> get(EntityType<@NotNull T> entityType) {
        return (EntityModel<T>) this.registry.get(entityType);
    }

    public void registerFinished(EntityType<?> value, Model finished) {
        this.finishedRegistry.put(value, finished);
    }

    public Model getFinished(EntityType<?> value) {
        return this.finishedRegistry.get(value);
    }

    public Collection<Model> getAll() {
        return this.finishedRegistry.values();
    }

    public Map<EntityType<?>, Model> getRegistry() {
        return Collections.unmodifiableMap(this.finishedRegistry);
    }

    @Override
    public void reload(ResourceManager resourceManager, ReloadContext context) {
        this.registry.clear();
        this.finishedRegistry.clear();

        Model somethingModel = this.blockBenchModel(new Identifier("entity/something"));
        this.registerFinished(EntityTypes.SOMETHING, somethingModel);

        // Call the onRegister method of the factory in ENTITY_MODELS
        ClientRegistrationEvents.ENTITY_MODELS.factory().onRegister();
    }

    private Model blockBenchModel(Identifier id) {
        return new BlockBenchModelImporter(id.mapPath(path -> "models/" + path + ".bbmodel")).createModel();
    }
}
