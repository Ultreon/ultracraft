package com.ultreon.craft.client.api.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.client.model.item.ItemModel;
import com.ultreon.craft.registry.RegistryKey;
import com.ultreon.craft.util.Identifier;

import java.util.List;
import java.util.Map;

public class JsonModel implements BlockModel, ItemModel {
    public final Map<String, Identifier> textureElements;
    public final List<JsonModelLoader.ModelElement> modelElements;
    public final boolean ambientOcclusion;
    public final JsonModelLoader.Display display;
    private final RegistryKey<?> key;
    private Model model;

    public JsonModel(RegistryKey<?> key, Map<String, Identifier> textureElements, List<JsonModelLoader.ModelElement> modelElements, boolean ambientOcclusion, JsonModelLoader.Display display) {
        this.key = key;
        this.textureElements = textureElements;
        this.modelElements = modelElements;
        this.ambientOcclusion = ambientOcclusion;
        this.display = display;
    }

    public Model bake() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
            JsonModelLoader.ModelElement modelElement = modelElements.get(i);
            modelElement.bake(i, modelBuilder, textureElements);
        }
        return modelBuilder.end();
    }

    @Override
    public void load(UltracraftClient client) {
        this.model = bake();
    }

    @Override
    public Identifier resourceId() {
        return key.element();
    }

    public RegistryKey<?> getKey() {
        return key;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void dispose() {
        if (model != null) {
            model.dispose();
        }
    }
}
