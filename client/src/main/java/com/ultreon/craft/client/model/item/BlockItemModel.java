package com.ultreon.craft.client.model.item;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.util.Identifier;

import java.util.function.Supplier;

public class BlockItemModel implements ItemModel {
    private final Supplier<BlockModel> blockModel;

    public BlockItemModel(Supplier<BlockModel> blockModel) {
        this.blockModel = blockModel;
    }

    @Override
    public void load(UltracraftClient client) {
        // Block models are loaded externally.
    }

    @Override
    public Identifier resourceId() {
        return this.blockModel.get().resourceId();
    }

    @Override
    public Model getModel() {
        return this.blockModel.get().getModel();
    }

    @Override
    public Vector3 getScale() {
        return this.blockModel.get().getItemScale();
    }

    @Override
    public Vector3 getOffset() {
        return this.blockModel.get().getItemOffset();
    }
}
