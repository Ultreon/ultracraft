package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class InputManager extends InputAdapter {
    private static final float DEG_PER_PIXEL = 0.6384300433839F;
    public int pauseKey = Input.Keys.ESCAPE;
    public int imGuiKey = Input.Keys.F3;
    public int imGuiFocusKey = Input.Keys.F4;
    private static final IntArraySet keys = new IntArraySet();
    private final UltreonCraft game;
    private int xPos;
    private int yPos;
    private int deltaX;
    private int deltaY;
    private boolean isCaptured;
    private boolean wasCaptured;

    public InputManager(UltreonCraft game, Camera camera) {
        this.game = game;
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.add(keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode);
        UltreonCraft craft = UltreonCraft.get();
        if (imGuiKey == keycode) {
            if (craft.isShowingImGui()) {
                Gdx.input.setCursorCatched(true);
            }
            craft.setShowingImGui(!craft.isShowingImGui());
        }
        if (imGuiFocusKey == keycode && craft.isShowingImGui()) {
            Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
        }
        return true;
    }

    public static boolean isKeyDown(int keycode) {
        return keys.contains(keycode);
    }
    
    public void update() {
        update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.wasCaptured = isCaptured;
        this.isCaptured = Gdx.input.isCursorCatched();

        if (!Gdx.input.isCursorCatched()) {
            return super.mouseMoved(screenX, screenY);
        }

        updatePlayerMovement(screenX, screenY);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.wasCaptured = isCaptured;
        this.isCaptured = Gdx.input.isCursorCatched();

        if (!Gdx.input.isCursorCatched()) return super.mouseMoved(screenX, screenY);

        updatePlayerMovement(screenX, screenY);
        return true;
    }

    private void updatePlayerMovement(int screenX, int screenY) {
        if (isCaptured && !wasCaptured) {
            this.deltaX = 0;
            this.deltaY = 0;
        } else if (isCaptured) {
            this.deltaX = xPos - screenX;
            this.deltaY = yPos - screenY;
        }

        this.xPos = screenX;
        this.yPos = screenY;

        Vector2 rotation = game.player.getRotation();
        rotation.add(deltaX * DEG_PER_PIXEL, deltaY * DEG_PER_PIXEL);
        game.player.setRotation(rotation);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!Gdx.input.isCursorCatched() && !UltreonCraft.get().isShowingImGui()) {
            Gdx.input.setCursorCatched(true);
            return true;
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    public void update(float deltaTime) {
        if (!Gdx.input.isCursorCatched()) return;

        if (isKeyDown(pauseKey) && Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(false);
        }
    }
}
