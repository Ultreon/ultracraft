package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.api.events.RenderEvents;
import com.ultreon.craft.client.config.Config;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.render.pipeline.RenderPipeline;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.ultreon.craft.client.UltracraftClient.LOGGER;

public class GameRenderer {
    private final UltracraftClient client;
    private final ModelBatch modelBatch;
    private final RenderPipeline pipeline;
    private final Vector2 tmp = new Vector2();
    private FrameBuffer depthFbo;
    private FrameBuffer fbo;
    private final RenderContext context;
    private float cameraBop = 0.0f;

    public GameRenderer(UltracraftClient client, ModelBatch modelBatch, RenderPipeline pipeline) {
        this.client = client;
        this.modelBatch = modelBatch;
        this.pipeline = pipeline;

        this.context = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN));

        this.depthFbo = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

//        ShaderProgram.pedantic = false;
        ShaderProgram worldShaderProgram = ShaderPrograms.MODEL;
        if (!worldShaderProgram.isCompiled()) {
            LOGGER.error("Failed to compile model shader:\n{}", worldShaderProgram.getLog());
        }
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
                this.client.camera.far = (Config.renderDistance - 1) * World.CHUNK_SIZE / WorldRenderer.SCALE;

                var rotation = this.tmp.set(player.xHeadRot, player.yRot);
                var quaternion = new Quaternion();
                quaternion.setFromAxis(Vector3.Y, rotation.x);
                quaternion.mul(new Quaternion(Vector3.X, rotation.y));
                quaternion.conjugate();

                // Add camera bop. Use easing and animate with cameraBop. Camera Bop is a sort of camera movement while walking.
                float cameraBop = calculateCameraBop(deltaTime);

                this.client.camera.up.set(0, 1, 0);
                this.client.camera.up.rotate(Vector3.Y, rotation.x);
                this.client.camera.up.rotate(Vector3.Z, cameraBop);
                this.client.camera.up.rotate(Vector3.Y, -rotation.x);
            });
        }

        if (this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {
            UltracraftClient.PROFILER.section("world", () -> {
                RenderEvents.PRE_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);
                this.renderWorld();
                RenderEvents.POST_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);
            });
        }

        renderer.begin();

        var screen = this.client.screen;


        renderer.pushMatrix();
        renderer.translate(this.client.getDrawOffset().x, this.client.getDrawOffset().y);
        renderer.scale(this.client.getGuiScale(), this.client.getGuiScale());
        UltracraftClient.PROFILER.section("overlay", () -> {
            this.renderOverlays(renderer, screen, world, deltaTime);

            if (this.client.crashOverlay != null) {
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.TAB)) {
                    this.client.crashOverlay.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
                } else {
                    this.client.crashOverlay.reset();
                }
            }
        });

        if (!this.client.isLoading()) {
            this.client.notifications.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
        }

        renderer.popMatrix();

        renderer.end();
    }

    /**
     * Calculates the camera bop movement based on the given deltaTime.
     *
     * @param deltaTime the time elapsed since the last frame
     * @return the calculated camera bop value
     */
    private float calculateCameraBop(float deltaTime) {
        float bop = this.cameraBop;
        if (bop > 0) {
            bop -= deltaTime * 2;
            if (bop < 0) bop = 0;
        } else if (bop < 0) {
            bop += deltaTime * 2;
            if (bop > 0) bop = 0;
        }

        return this.cameraBop = bop;
    }

    private void renderWorld() {
        this.pipeline.render(this.modelBatch);
    }

    private void renderOverlays(Renderer renderer, @Nullable Screen screen, @Nullable World world, float deltaTime) {
        if (world != null) {
            UltracraftClient.PROFILER.section("hud", () -> {
                if (this.client.hideHud) return;
                this.client.hud.render(renderer, deltaTime);
                RenderEvents.RENDER_HUD.factory().onRenderHud(this.client.hud, renderer, deltaTime);
            });
        }

        if (screen != null) {
            UltracraftClient.PROFILER.section("screen", () -> {
                float x = (Gdx.input.getX() - this.client.getDrawOffset().x) / this.client.getGuiScale();
                float y = (Gdx.input.getY() + this.client.getDrawOffset().y) / this.client.getGuiScale();
                RenderEvents.PRE_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, x, y, deltaTime);
                screen.render(renderer, (int) x, (int) y, deltaTime);
                RenderEvents.POST_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, x, y, deltaTime);
            });
        }

        UltracraftClient.PROFILER.section("debug", () -> {
            if (this.client.hideHud || this.client.isLoading()) return;
            this.client.debugGui.render(renderer);
        });
    }

    public RenderContext getContext() {
        return this.context;
    }
}
