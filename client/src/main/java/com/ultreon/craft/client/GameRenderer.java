package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.badlogic.gdx.graphics.GL20.*;

public class GameRenderer {
    private final UltracraftClient client;
    private final ModelBatch modelBatch;
    private final SpriteBatch spriteBatch;

    public GameRenderer(UltracraftClient client, ModelBatch modelBatch, SpriteBatch spriteBatch) {
        this.client = client;
        this.modelBatch = modelBatch;
        this.spriteBatch = spriteBatch;
    }

    public void render(Renderer renderer, float deltaTime) {
        var world = this.client.world;
        var worldRenderer = this.client.worldRenderer;

        LocalPlayer player = this.client.player;

        if (player != null) {
            UltracraftClient.PROFILER.section("camera", () -> {
                if (this.client.screen == null && !ImGuiOverlay.isShown()) {
                    player.rotate(-Gdx.input.getDeltaX() / 2f, -Gdx.input.getDeltaY() / 2f);
                }

                this.client.camera.update(player);
                this.client.camera.far = (this.client.settings.renderDistance.get() - 1) * World.CHUNK_SIZE;

                var rotation = player.getRotation();
                var quaternion = new Quaternion();
                quaternion.setFromAxis(Vector3.Y, rotation.x);
                quaternion.mul(new Quaternion(Vector3.X, rotation.y));
                quaternion.conjugate();
            });
        }

        if (this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {
            UltracraftClient.PROFILER.section("world", () -> {
                ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
                Gdx.gl20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                this.modelBatch.begin(this.client.camera);
                this.modelBatch.getRenderContext().setCullFace(UltracraftClient.CULL_FACE);
                this.modelBatch.getRenderContext().setDepthTest(GL_DEPTH_FUNC);
                this.modelBatch.render(worldRenderer::draw, worldRenderer.getEnvironment());
                this.modelBatch.end();
            });
        }

        this.spriteBatch.begin();

        var screen = this.client.screen;


        renderer.pushMatrix();
        renderer.translate(this.client.getDrawOffset().x, this.client.getDrawOffset().y);
        renderer.scale(this.client.getGuiScale(), this.client.getGuiScale());
        UltracraftClient.PROFILER.section("overlay", () -> {
            this.renderOverlays(renderer, screen, world, deltaTime);

            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) &&
                    Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) &&
                    Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
                this.client.crashOverlay.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
            } else {
                this.client.crashOverlay.reset();
            }
        });
        renderer.popMatrix();

        this.spriteBatch.end();
    }

    private void renderOverlays(Renderer renderer, @Nullable Screen screen, @Nullable World world, float deltaTime) {
        if (world != null) {
            UltracraftClient.PROFILER.section("hud", () -> {
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

        UltracraftClient.PROFILER.section("debug", () -> {
            this.client.debugRenderer.render(renderer);
        });
    }
}
