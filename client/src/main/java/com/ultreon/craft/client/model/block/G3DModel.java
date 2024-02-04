package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceLoader;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.BlockPos;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class G3DModel implements BlockModel {
    private final ElementID resource;
    private final ModelConfig config;
    private Model model;

    public G3DModel(ElementID resource) {
        this(resource, new ModelConfig());
    }

    public G3DModel(ElementID resource, ModelConfig config) {
        this.resource = resource;
        this.config = config;
    }

    @Override
    public void load(UltracraftClient client) {
        this.model = client.deferDispose(ResourceLoader.loadG3D(this.resource));
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
        Model model = this.model;
        var instance = new ModelInstance(model);
        instance.transform.setToTranslationAndScaling(new Vector3(config.translation), new Vector3().add(config.scale));
        chunk.addModel(pos, instance);
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public void dispose() {
        this.model.dispose();
    }

    public static class ModelConfig {
        public static final ModelConfig BLOCKBENCH = new ModelConfig().scale(1 / 100f);

        private final Vector3 scale = new Vector3(1, 1, 1);
        private final Vector3 translation = new Vector3();

        public ModelConfig scale(float x, float y, float z) {
            this.scale.set(x, y, z);
            return this;
        }

        public ModelConfig scale(Vector3 scale) {
            this.scale.set(scale);
            return this;
        }

        public ModelConfig scale(float scale) {
            this.scale.set(scale, scale, scale);
            return this;
        }

        public ModelConfig translation(float x, float y, float z) {
            this.translation.set(x, y, z);
            return this;
        }

        public ModelConfig translation(Vector3 translation) {
            this.translation.set(translation);
            return this;
        }
    }
}
