package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.google.common.collect.Table;
import com.ultreon.craft.block.state.BlockDataEntry;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.client.model.item.ItemModel;
import com.ultreon.craft.registry.RegistryKey;
import com.ultreon.craft.util.Identifier;

import java.util.List;
import java.util.Map;

public class Json5Model implements BlockModel, ItemModel {
    public final Map<String, Identifier> textureElements;
    public final List<Json5ModelLoader.ModelElement> modelElements;
    public final boolean ambientOcclusion;
    public final Json5ModelLoader.Display display;
    private final RegistryKey<?> key;
    private Model model;
    private Table<String, BlockDataEntry<?>, Json5Model> overrides;

    public Json5Model(RegistryKey<?> key, Map<String, Identifier> textureElements, List<Json5ModelLoader.ModelElement> modelElements, boolean ambientOcclusion, Json5ModelLoader.Display display, Table<String, BlockDataEntry<?>, Json5Model> overrides) {
        this.key = key;
        this.textureElements = textureElements;
        this.modelElements = modelElements;
        this.ambientOcclusion = ambientOcclusion;
        this.display = display;
        this.overrides = overrides;
    }

    public Model bake() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
            Json5ModelLoader.ModelElement modelElement = modelElements.get(i);
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

    public Table<String, BlockDataEntry<?>, Json5Model> getOverrides() {
        return this.overrides;
    }
}
