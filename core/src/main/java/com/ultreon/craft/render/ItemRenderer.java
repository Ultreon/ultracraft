package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.render.model.BakedCubeModel;

public class ItemRenderer {
    private final UltreonCraft game;
    private final ModelBatch batch;
    private final OrthographicCamera orthoCam;
    private Material material;

    public ItemRenderer(UltreonCraft game) {
        this.game = game;
        this.batch = new ModelBatch();
        this.orthoCam = new OrthographicCamera(game.getScaledWidth(), game.getScaledHeight());
        this.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.game.blocksTextureAtlas.getTexture()));
        this.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
    }

    public void render(Block block, Renderer renderer, int x, int y) {
        renderer.flush();
        this.batch.begin(this.orthoCam);
        BakedCubeModel bakedBlockModel = this.game.getBakedBlockModel(block);
        if (bakedBlockModel == null) return;
        Mesh mesh = bakedBlockModel.getMesh();
        Renderable renderable = new Renderable();
        renderable.meshPart.mesh = mesh;
        renderable.meshPart.center.set(0.5F, 0.5F, 0.5F);
        renderable.material = this.material;
        renderable.worldTransform.translate(x, y, 0);
        renderable.worldTransform.rotate(Vector3.X, 30);
        renderable.worldTransform.rotate(Vector3.Y, 45);
        renderable.worldTransform.scale(8, 8, 8);
        this.batch.render(renderable);
        this.batch.end();
    }
}
