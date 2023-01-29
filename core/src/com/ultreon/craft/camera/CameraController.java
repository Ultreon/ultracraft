package com.ultreon.craft.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class CameraController extends InputAdapter {
    private static final float DEGREES_PER_PIXEL = 0.5f;
    private final Camera camera;
    public int forwardKey = Input.Keys.W;
    public int strafeLeftKey = Input.Keys.A;
    public int backwardKey = Input.Keys.S;
    public int strafeRightKey = Input.Keys.D;
    public int upKey = Input.Keys.SPACE;
    public int downKey = Input.Keys.SHIFT_LEFT;
    public int pauseKey = Input.Keys.ESCAPE;
    public int runningKey = Input.Keys.CONTROL_LEFT;
    private final IntArraySet keys = new IntArraySet();
    private final Vector3 tmp = new Vector3();
    private float speed = 5;
    private float runningSpeed = 10;

    public CameraController(Camera camera) {
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

    public boolean isKeyDown(int keycode) {
        return keys.contains(keycode);
    }
    
    public void update() {
        update(Gdx.graphics.getDeltaTime());
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getRunningSpeed() {
        return runningSpeed;
    }

    public void setRunningSpeed(float runningSpeed) {
        this.runningSpeed = runningSpeed;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (!Gdx.input.isCursorCatched()) return super.mouseMoved(screenX, screenY);
        float deltaX = -Gdx.input.getDeltaX() * DEGREES_PER_PIXEL;
        float deltaY = -Gdx.input.getDeltaY() * DEGREES_PER_PIXEL;
        camera.direction.rotate(camera.up, deltaX);
        tmp.set(camera.direction).crs(camera.up).nor();
        camera.direction.rotate(tmp, deltaY);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!Gdx.input.isCursorCatched()) return super.mouseMoved(screenX, screenY);
        float deltaX = -Gdx.input.getDeltaX() * DEGREES_PER_PIXEL;
        float deltaY = -Gdx.input.getDeltaY() * DEGREES_PER_PIXEL;
        camera.direction.rotate(camera.up, deltaX);
        tmp.set(camera.direction).crs(camera.up).nor();
        camera.direction.rotate(tmp, deltaY);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(true);
            return true;
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    public void update(float deltaTime) {
        if (!Gdx.input.isCursorCatched()) return;

        var speed = isKeyDown(runningKey) ? getRunningSpeed() : getSpeed();

        if (isKeyDown(forwardKey)) {
            tmp.set(camera.direction).nor().scl(deltaTime * speed);
            camera.position.add(tmp);
        }
        if (isKeyDown(backwardKey)) {
            tmp.set(camera.direction).nor().scl(-deltaTime * speed);
            camera.position.add(tmp);
        }
        if (isKeyDown(strafeLeftKey)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(-deltaTime * speed);
            camera.position.add(tmp);
        }
        if (isKeyDown(strafeRightKey)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(deltaTime * speed);
            camera.position.add(tmp);
        }
        if (isKeyDown(upKey)) {
            tmp.set(camera.up).nor().scl(deltaTime * speed);
            camera.position.add(tmp);
        }
        if (isKeyDown(downKey)) {
            tmp.set(camera.up).nor().scl(-deltaTime * speed);
            camera.position.add(tmp);
        }
        if (isKeyDown(pauseKey) && Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(false);
        }
        camera.update(true);
    }
}
