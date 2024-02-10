package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.render.pipeline.RenderPipeline;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.ultreon.craft.client.UltracraftClient.LOGGER;

public class GameRenderer {
    private final UltracraftClient client;
    private final ModelBatch modelBatch;
    private final RenderPipeline pipeline;
    private final Vector2 tmp = new Vector2();
    private final ShaderProgram worldShaderProgram;
    private FrameBuffer depthFbo;
    private FrameBuffer fbo;
    private final RenderContext context;
    private final Mesh quad;
    private final Matrix4 identityMatrix = new Matrix4();
    private Quaternion tmpQ = new Quaternion();
    private float cameraBop = 0.0f;
    private boolean revert = true;

    public GameRenderer(UltracraftClient client, ModelBatch modelBatch, RenderPipeline pipeline) {
        this.client = client;
        this.modelBatch = modelBatch;
        this.pipeline = pipeline;

        this.context = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN));

        this.depthFbo = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

//        ShaderProgram.pedantic = false;
        this.worldShaderProgram = ShaderPrograms.MODEL;
        if (!this.worldShaderProgram.isCompiled()) {
            LOGGER.error("Failed to compile model shader:\n{}", this.worldShaderProgram.getLog());
        }

        this.quad = this.createFullScreenQuad();
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        this.depthFbo.dispose();
        this.fbo.dispose();
        this.depthFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.pipeline.resize(width, height);
    }

    public void render(Renderer renderer, float deltaTime) {
        var world = this.client.world;
        var worldRenderer = this.client.worldRenderer;

        LocalPlayer player = this.client.player;

        if (player != null) {
            UltracraftClient.PROFILER.section("camera", () -> {
                if (this.client.screen == null && !ImGuiOverlay.isShown()) {
                    player.rotateHead(-Gdx.input.getDeltaX() / 2f, -Gdx.input.getDeltaY() / 2f);
                }

                this.client.camera.update(player);
                this.client.camera.far = (this.client.config.get().renderDistance - 1) * World.CHUNK_SIZE / WorldRenderer.SCALE;

                var rotation = this.tmp.set(player.xHeadRot, player.yRot);
                var quaternion = new Quaternion();
                quaternion.setFromAxis(Vector3.Y, rotation.x);
                quaternion.mul(new Quaternion(Vector3.X, rotation.y));
                quaternion.conjugate();

//                float genSpeed = 35.0f;
//                float speed = (genSpeed / (17.5f - (cameraBop * 2.0f))) * (1 - (Math.max(Math.abs(rotation.y) - 45, 0)) / 45);
//                cameraBop += Gdx.graphics.getDeltaTime() * (revert ? -speed : speed);
//                if (cameraBop > 4.0f) revert = true;
//                else if (cameraBop < -4.0f) revert = false;

                this.client.camera.up.set(0, 1, 0);
                this.client.camera.up.rotate(Vector3.Y, rotation.x);
                this.client.camera.up.rotate(Vector3.Z, cameraBop);
                this.client.camera.up.rotate(Vector3.Y, -rotation.x);
            });
        }

        if (this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {
            UltracraftClient.PROFILER.section("world", () -> this.renderWorld(worldRenderer, world));
        }

        renderer.begin();

        var screen = this.client.screen;


        renderer.pushMatrix();
        renderer.translate(this.client.getDrawOffset().x, this.client.getDrawOffset().y);
        renderer.scale(this.client.getGuiScale(), this.client.getGuiScale());
        UltracraftClient.PROFILER.section("overlay", () -> {
            this.renderOverlays(renderer, screen, world, deltaTime);

            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.TAB)) {
                this.client.crashOverlay.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
            } else {
                this.client.crashOverlay.reset();
            }
        });

        if (!this.client.isLoading()) {
            this.client.notifications.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
        }

        renderer.popMatrix();

        renderer.end();
    }

    private void renderWorld(WorldRenderer worldRenderer, ClientWorld world) {
        this.pipeline.render(this.modelBatch);
    }

    private void renderWorld0(WorldRenderer worldRenderer, ClientWorld world) {
        LocalPlayer localPlayer = this.client.player;
        if (localPlayer == null) return;
        Vec3d position = localPlayer.getPosition();

        this.fbo.begin();
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        this.renderWorldOnce(worldRenderer, world, position, this.modelBatch);
        this.fbo.end();
        this.fbo.getColorBufferTexture().bind(0);

        this.context.begin();

        this.depthFbo.begin();
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        this.renderWorldOnce(worldRenderer, world, position, this.modelBatch);
        this.depthFbo.end();
        this.depthFbo.getColorBufferTexture().bind(1);

        this.worldShaderProgram.bind();
        this.worldShaderProgram.setUniformi("u_diffuseTexture", 0);
        this.worldShaderProgram.setUniformi("u_depthTexture", 1);
        this.worldShaderProgram.setUniformMatrix("u_inverseProjectionMatrix", this.client.camera.invProjectionView);
        this.worldShaderProgram.setUniformMatrix("u_projTrans", this.client.camera.projection);
        this.worldShaderProgram.setUniformMatrix("u_viewTrans", this.client.camera.view);
        this.worldShaderProgram.setUniformMatrix("u_worldTrans", this.identityMatrix);
        this.worldShaderProgram.setUniformf("u_resolution", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        this.quad.render(this.worldShaderProgram, GL_TRIANGLE_FAN);
        this.context.end();
    }

    public Mesh createFullScreenQuad() {
        float[] vertices = new float[20];
        int i = 0;

        vertices[i++] = -1;
        vertices[i++] = -1;
        vertices[i++] = 0;
        vertices[i++] = 0f;
        vertices[i++] = 0f;

        vertices[i++] = 1f;
        vertices[i++] = -1;
        vertices[i++] = 0;
        vertices[i++] = 1f;
        vertices[i++] = 0f;

        vertices[i++] = 1f;
        vertices[i++] = 1f;
        vertices[i++] = 0;
        vertices[i++] = 1f;
        vertices[i++] = 1f;

        vertices[i++] = -1;
        vertices[i++] = 1f;
        vertices[i++] = 0;
        vertices[i++] = 0f;
        vertices[i] = 1f;

        Mesh mesh = new Mesh(true, 4, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );

        mesh.setVertices(vertices);
        return mesh;
    }

    private void renderWorldOnce(WorldRenderer worldRenderer, ClientWorld world, Vec3d position, ModelBatch batch) {
        worldRenderer.renderEntities();

        batch.begin(this.client.camera);

        world.getAllEntities().stream().sorted((e1, e2) -> {
            var d1 = e1.getPosition().dst(position);
            var d2 = e2.getPosition().dst(position);
            return Double.compare(d1, d2);
        }).forEachOrdered(entity -> batch.render((output, pool) -> worldRenderer.collectEntity(entity, output, pool)));

        batch.render(worldRenderer::collect, worldRenderer.getEnvironment());

        UltracraftClient.PROFILER.section("(Local Player)", () -> {
            LocalPlayer localPlayer = this.client.player;
            if (localPlayer == null || !this.client.isInThirdPerson()) return;

            batch.render((output, pool) -> worldRenderer.collectEntity(localPlayer, output, pool));
        });
        batch.end();
    }

    private void renderOverlays(Renderer renderer, @Nullable Screen screen, @Nullable World world, float deltaTime) {
        if (world != null) {
            UltracraftClient.PROFILER.section("hud", () -> {
                if (this.client.hideHud) return;
                this.client.hud.render(renderer, deltaTime);
            });
        }

        if (screen != null) {
            UltracraftClient.PROFILER.section("screen", () -> {
                float x = (Gdx.input.getX() - this.client.getDrawOffset().x) / this.client.getGuiScale();
                float y = (Gdx.input.getY() + this.client.getDrawOffset().y) / this.client.getGuiScale();
                screen.render(renderer, (int) x, (int) y, deltaTime);
            });
        }

        if (this.client.gamepadInput.isVirtualKeyboardOpen()) {
            this.client.virtualKeyboard.render(renderer, (int) (Gdx.input.getX() / this.client.getGuiScale()), (int) (Gdx.input.getY() / this.client.getGuiScale()), deltaTime);
        }

        this.client.gamepadHud.render(renderer, deltaTime);

        UltracraftClient.PROFILER.section("debug", () -> {
            if (this.client.hideHud) return;
            this.client.debugGui.render(renderer);
        });
    }

    public RenderContext getContext() {
        return this.context;
    }
}
