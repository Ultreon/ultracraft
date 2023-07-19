package com.ultreon.craft.render.entity;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.ItemEntity;
import com.ultreon.craft.render.model.BakedCubeModel;

public class ItemEntityRenderer<T extends ItemEntity> extends EntityRenderer<T> {
    public ItemEntityRenderer(EntityModelContext context) {
        super(context);
    }

    @Override
    public void build() {
        EntityModelContext context = this.getContext();
        context.box("cube", -.2F, -.2F, -.2F, .4F, .4F, .4F);
    }

    @Override
    protected void setAngles(T entity) {
        float spin = entity.getSpin();
        this.getContext().setRotation("cube", new Quaternion(Vector3.Y, spin));
//        this.getContext().setOffset("cube", new Vector3(entity.getAge() % 30));
    }

    @Override
    protected Material getMaterial(T entity) {
        Block item = entity.getItem();
        BakedCubeModel bakedBlockModel = UltreonCraft.get().getBakedBlockModel(item);
        if (bakedBlockModel == null) return new Material();

        return new Material(TextureAttribute.createDiffuse(bakedBlockModel.top()));
    }
}
