package com.ultreon.craft.client.model.item;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.util.ElementID;

public class FlatItemModel implements ItemModel {
    private final Item item;
    private Model model;

    public FlatItemModel(Item item) {
        this.item = item;
    }

    @Override
    public void load(UltracraftClient client) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Material material = new Material(item.getId().toString());
        // Todo use MeshBuilder
//        modelBuilder.createRect(0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, GL20.GL_TRIANGLES, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
//        modelBuilder.createRect(0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, -1, GL20.GL_TRIANGLES, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        this.model = modelBuilder.end();
    }

    @Override
    public ElementID resourceId() {
        return item.getId();
    }

    @Override
    public Model getModel() {
        return model;
    }

    public Item getItem() {
        return item;
    }
}
