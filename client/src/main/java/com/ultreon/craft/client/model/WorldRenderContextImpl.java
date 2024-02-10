package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public class WorldRenderContextImpl<T> implements Disposable, WorldRenderContext<T> {
    final Array<Renderable> output;
    final Pool<Renderable> pool;
    final T holder;
    private final World world;
    private final float worldScale;
    private final Vec3d cameraPos;

    public WorldRenderContextImpl(Array<Renderable> output, Pool<Renderable> pool, T holder, World world, float worldScale, Vec3d cameraPos) {
        this.output = output;
        this.pool = pool;
        this.holder = holder;
        this.world = world;
        this.worldScale = worldScale;
        this.cameraPos = cameraPos;
    }

    @Override
    public T getHolder() {
        return holder;
    }

    @Override
    public void render(RenderableProvider renderableProvider) {
        renderableProvider.getRenderables(output, pool);
    }

    @Override
    public void dispose() {
        pool.clear();
        output.clear();
    }

    public Vec3d relative(Vec3d translation, Vec3d tmp) {
        return tmp.set(0).add(translation).sub(cameraPos).mul(1.0 / worldScale);
    }

    @Override
    public World getWorld() {
        return world;
    }
}
