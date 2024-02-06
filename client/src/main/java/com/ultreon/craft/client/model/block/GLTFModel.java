package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceLoader;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.BlockPos;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public class GLTFModel implements BlockModel {
    private final ElementID resource;
    private SceneAsset asset;

    public GLTFModel(ElementID resource) {
        this.resource = resource;
    }

    @Override
    public void load(UltracraftClient client) {
        this.asset = client.deferDispose(ResourceLoader.loadGLTF(this.resource));
    }

    @Override
    public ElementID resourceId() {
        return this.resource;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public void loadInto(BlockPos pos, ClientChunk chunk) {
        Model model = this.asset.scene.model;
        var instance = new ModelInstance(model, "GLTF_MODEL@" + resource.toString());
        chunk.addModel(pos, instance);
    }

    @Override
    public Model getModel() {
        return this.asset.scene.model;
    }

    public SceneAsset getAsset() {
        return this.asset;
    }

    @Override
    public void dispose() {
        this.asset.dispose();
    }
}
