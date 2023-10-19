package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.Hud;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Ray;
import com.ultreon.craft.world.World;

import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.Nullable;

public class MobileInput extends GameInput {
    private int rotatePointer = -1;
    private GridPoint2 rotateOrigin;
    private HitResult inputHitResult;
    private float pressTime;
    private boolean pressing;
    private int pressPointer;
    private final Vector2 pressPos = new Vector2();

    public MobileInput(UltreonCraft game, GameCamera camera) {
        super(game, camera);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK) {
            Screen currentScreen = this.game.currentScreen;
            if (currentScreen != null) {
                currentScreen.back();
            } else if (this.game.isPlaying()) {
                this.game.pause();
            }
        }
        return super.keyDown(keycode);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        inGame: if (this.game.isPlaying()) {
            if (this.rotatePointer == -1 && this.isUsing() && this.pressPointer == pointer && this.pressPos.dst(screenX, screenY) >= 20) {
                this.pressing = false;
                this.rotateOrigin = new GridPoint2(screenX, screenY);
                this.rotatePointer = pointer;
                return true;
            }

            Hud hud = this.game.hud;
            if (hud.touchDragged(screenX, screenY, pointer)) {
                break inGame;
            }

            Player player = this.game.player;
            if (player != null && this.rotatePointer == pointer) {
                this.updatePlayerMovement(player, screenX, screenY, pointer);
            }
        }
        return true;
    }

    private void updatePlayerMovement(Player player, int screenX, int screenY, int pointer) {

    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        World world = this.game.world;
        if (this.game.isPlaying() && world != null) {
            Hud hud = this.game.hud;
            if (hud.touchDown(screenX, screenY, pointer)) {
                return true;
            }

            if (pointer == 0) {
                this.pressing = true;
                this.pressTime = 0.0F;
                this.pressPointer = pointer;
                this.pressPos.set(screenX, screenY);
            }
        } else {
            Screen currentScreen = this.game.currentScreen;
            return currentScreen != null && currentScreen.mousePress((int) (screenX / this.game.getGuiScale()), (int) (screenY / this.game.getGuiScale()), button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) this.pressing = false;

        if (this.game.isPlaying()) {
            if (this.rotatePointer == pointer) {
                this.rotatePointer = -1;
                this.rotateOrigin = null;
                return true;
            }

            Hud hud = this.game.hud;
            hud.touchUp(screenX, screenY, pointer);

            World world = this.game.world;
            if (world != null && this.pressTime < 0.5F) {
                this.onWorldHit(world, this.game.player, this.inputHitResult, true, false);
            }
        } else {
            Screen currentScreen = this.game.currentScreen;
            if (currentScreen != null) {
                currentScreen.mouseRelease((int) (screenX / this.game.getGuiScale()), (int) (screenY / this.game.getGuiScale()), button);
                currentScreen.mouseClick((int) (screenX / this.game.getGuiScale()), (int) (screenY / this.game.getGuiScale()), button, 1);
            }
        }
        return false;
    }

    public Vec2i getTouchPos() {
        float x = Gdx.input.getX(0) / this.game.getGuiScale();
        float y = (this.game.getHeight() - Gdx.input.getY(0)) / this.game.getGuiScale();
        return new Vec2i((int) x, (int) y);
    }

    public Ray getInputRay() {
        float viewportWidth = this.camera.viewportWidth;
        float viewportHeight = this.camera.viewportHeight;

        float viewportX = (2.0f * Gdx.input.getX(0)) / viewportWidth - 1.0f;
        float viewportY = (2.0f * (viewportHeight - Gdx.input.getY(0))) / viewportHeight - 1.0f;

        Vector3 gdxOrigin = new Vector3();
        gdxOrigin.set(viewportX, viewportY, -1.0f);
        gdxOrigin.prj(this.camera.invProjectionView);

        Vector3 gdxDirection = new Vector3();
        gdxDirection.set(viewportX, viewportY, 1.0f);
        gdxDirection.prj(this.camera.invProjectionView);
        gdxDirection.sub(gdxOrigin).nor();

        return new Ray(new Vec3d(gdxOrigin.x, gdxOrigin.y, gdxOrigin.z), new Vec3d(gdxDirection.x, gdxDirection.y, gdxDirection.z));
    }

    public boolean isDestroyMode() {
        return this.pressTime >= 0.5F;
    }

    public boolean isDestroying() {
        return this.isDestroyMode() && this.pressing;
    }

    public boolean isUsing() {
        return !this.isDestroyMode() && this.pressing;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (this.pressing) {
            this.pressTime += deltaTime;
        }

        Ray inputRay = this.getInputRay();
        @Nullable World world = this.game.world;
        if (world != null) {
            this.inputHitResult = world.rayCast(inputRay);
            if (this.isDestroying()) {
                this.onWorldHit(world, this.game.player, this.inputHitResult, true, false);
            }
        }
    }
}
