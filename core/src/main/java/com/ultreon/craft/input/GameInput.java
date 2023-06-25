package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.World;

import it.unimi.dsi.fastutil.ints.IntArraySet;

public abstract class GameInput implements InputProcessor {
    protected static final float DEG_PER_PIXEL = 0.6384300433839F;
    private static final IntArraySet keys = new IntArraySet();
    protected final UltreonCraft game;
    protected final Camera camera;

    public GameInput(UltreonCraft game, Camera camera) {
        this.game = game;
        this.camera = camera;
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.add(keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode);
        return true;
    }

    public static boolean isKeyDown(int keycode) {
        return keys.contains(keycode);
    }
    
    public void update() {
        this.update(Gdx.graphics.getDeltaTime());
    }

    public void update(float deltaTime) {

    }
}
