package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.collection.PaletteContainer;
import com.ultreon.craft.render.model.BakedCubeModel;

public class ItemRenderer {
    private final UltreonCraft game;
    private final Environment environment;
    private final ModelBatch batch;
    private final OrthographicCamera orthoCam;
    private final Material material;
    private final Quaternion quaternion = new Quaternion();
    private final Vector3 rotation = new Vector3(-30, 45, 0);
    private final Vector3 position = new Vector3(0, 0, -1000);
    private final Vector3 scale = new Vector3(20, 20, 20);
    protected final Vector3 tmp = new Vector3();

    public ItemRenderer(UltreonCraft game, Environment environment) {
        this.game = game;
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
        this.environment.add(new DirectionalLight().set(.8f, .8f, .8f, this.tmp.set(.8f, 0, -.6f).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(.8f, .8f, .8f, this.tmp.set(-.8f, 0, .6f).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, this.tmp.set(0, -1, 0).rotate(Vector3.Y, 45)));
        this.environment.add(new DirectionalLight().set(0.17f, .17f, .17f, this.tmp.set(0, 1, 0).rotate(Vector3.Y, 45)));
        this.batch = new ModelBatch();
        this.orthoCam = new OrthographicCamera(game.getScaledWidth(), game.getScaledHeight());
        this.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.game.blocksTextureAtlas.getTexture()));
        this.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
    }

    public void render(Block block, Renderer renderer, int x, int y) {
        renderer.model(() -> {
            float guiScale = this.game.getGuiScale();
            this.orthoCam.zoom = 32.0F / 8.0F / guiScale;
            this.orthoCam.far = 100000;
            this.orthoCam.update();
            this.batch.begin(this.orthoCam);
            BakedCubeModel bakedBlockModel = this.game.getBakedBlockModel(block);
            if (bakedBlockModel == null) return;
            Mesh mesh = bakedBlockModel.getMesh();
            Renderable renderable = new Renderable();
            renderable.meshPart.mesh = mesh;
            renderable.meshPart.center.set(0F, 0F, 0F);
            renderable.meshPart.offset = 0;
            renderable.meshPart.size = mesh.getMaxVertices();
            renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
            renderable.material = this.material;
            renderable.environment = this.environment;
            renderable.worldTransform.set(this.position.cpy().add((x - (int)(this.game.getScaledWidth() / 2.0F)) * guiScale, -(-y + (int)(this.game.getScaledHeight() / 2.0F)) * guiScale, 0), this.quaternion, this.scale);
            renderable.worldTransform.rotate(Vector3.X, this.rotation.x);
            renderable.worldTransform.rotate(Vector3.Y, this.rotation.y);
            this.batch.render(renderable);
            this.batch.end();
        });
    }

    public OrthographicCamera getOrthoCam() {
        return this.orthoCam;
    }
}
