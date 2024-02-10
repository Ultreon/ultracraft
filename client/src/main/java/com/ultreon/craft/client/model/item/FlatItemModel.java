package com.ultreon.craft.client.model.item;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;
import org.lwjgl.opengl.GL20;

public class FlatItemModel implements ItemModel {
    private final Item item;
    private Model model;

    public FlatItemModel(Item item) {
        this.item = item;
    }

    @Override
    public void load(UltracraftClient client) {
        ModelBuilder modelBuilder = new ModelBuilder();

        Material material = new Material("[REGISTRY] " + Registries.ITEM.getKey(item).toString());
        material.set(TextureAttribute.createDiffuse(UltracraftClient.get().itemTextureAtlas.get(new ElementID(item.getId().namespace(), "textures/items/" + item.getId().path() + ".png"))));
        material.set(TextureAttribute.createEmissive(UltracraftClient.get().itemTextureAtlas.get(new ElementID(item.getId().namespace(), "textures/items/" + item.getId().path() + ".png"))));
        material.set(ColorAttribute.createDiffuse(1f, 1f, 1f, 1f));
        material.set(ColorAttribute.createAmbient(1f, 1f, 1f, 1f));
        material.set(new BlendingAttribute());
        material.set(FloatAttribute.createAlphaTest(0.02f));
        modelBuilder.begin();

        MeshBuilder meshBuilder = new MeshBuilder();
        VertexAttribute[] attributes = {VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)};
        meshBuilder.begin(new VertexAttributes(attributes), GL20.GL_TRIANGLES);
        meshBuilder.setUVRange(0f, 0f, 1f, 1f);
        meshBuilder.setColor(1f, 1f, 1f, 1f);
        BoxShapeBuilder.build(meshBuilder, -0.5f, 0f, 0.5f, 1f, 1f, 0f);
        modelBuilder.part("plane", meshBuilder.end(), GL20.GL_TRIANGLES, material);
        model = modelBuilder.end();
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
